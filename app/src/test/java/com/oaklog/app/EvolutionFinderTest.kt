package com.oaklog.app

import com.oaklog.app.data.remote.dto.ChainLinkDto
import com.oaklog.app.data.remote.dto.EvolutionDetailDto
import com.oaklog.app.data.remote.dto.NamedRefDto
import com.oaklog.app.data.repository.EvolutionMapper
import com.oaklog.app.domain.logic.EvolutionFinder
import com.oaklog.app.domain.model.EvoMethod
import com.oaklog.app.domain.model.EvolutionStep
import org.junit.Assert.assertEquals
import org.junit.Test

class EvolutionFinderTest {

    private fun ref(name: String, id: Int) = NamedRefDto(name, "https://pokeapi.co/api/v2/pokemon-species/$id/")

    private val eevee = ChainLinkDto(
        species = ref("eevee", 133),
        evolvesTo = listOf(
            ChainLinkDto(
                species = ref("vaporeon", 134),
                evolutionDetails = listOf(EvolutionDetailDto(item = NamedRefDto("water-stone"), trigger = NamedRefDto("use-item"))),
            ),
            ChainLinkDto(
                species = ref("umbreon", 197),
                evolutionDetails = listOf(EvolutionDetailDto(minHappiness = 220, timeOfDay = "night", trigger = NamedRefDto("level-up"))),
            ),
            ChainLinkDto(
                species = ref("sylveon", 700),
                evolutionDetails = listOf(EvolutionDetailDto(knownMoveType = NamedRefDto("fairy"), minAffection = 2)),
            ),
        ),
    )

    @Test
    fun `chain flattens into one step per evolution with the right category`() {
        val steps = EvolutionMapper.steps(eevee).associateBy { it.toName }
        assertEquals(EvoMethod.ITEM, steps.getValue("vaporeon").method)
        assertEquals("water-stone", steps.getValue("vaporeon").item)
        assertEquals(EvoMethod.HAPPINESS, steps.getValue("umbreon").method)
        assertEquals(EvoMethod.MOVE, steps.getValue("sylveon").method)
        assertEquals(133, steps.getValue("umbreon").fromId)
    }

    @Test
    fun `a stone outranks a level requirement it is paired with`() {
        val d = EvolutionDetailDto(item = NamedRefDto("fire-stone"), minLevel = 1)
        assertEquals(EvoMethod.ITEM, EvolutionMapper.method(d))
    }

    @Test
    fun `a species with two routes yields two steps`() {
        val chain = ChainLinkDto(
            species = ref("kirlia", 281),
            evolvesTo = listOf(
                ChainLinkDto(
                    species = ref("gallade", 475),
                    evolutionDetails = listOf(
                        EvolutionDetailDto(item = NamedRefDto("dawn-stone")),
                        EvolutionDetailDto(minLevel = 30),
                    ),
                ),
            ),
        )
        assertEquals(2, EvolutionMapper.steps(chain).size)
    }

    private val steps = listOf(
        EvolutionStep(133, "eevee", 134, "vaporeon", EvoMethod.ITEM, "water-stone", "Use Water Stone"),
        EvolutionStep(133, "eevee", 135, "jolteon", EvoMethod.ITEM, "thunder-stone", "Use Thunder Stone"),
        EvolutionStep(25, "pikachu", 26, "raichu", EvoMethod.ITEM, "thunder-stone", "Use Thunder Stone"),
        EvolutionStep(4, "charmander", 5, "charmeleon", EvoMethod.LEVEL, null, "Level 16"),
    )

    @Test
    fun `filtering by item finds everything that uses it, in dex order`() {
        val result = EvolutionFinder.filter(steps, null, "thunder-stone", "")
        assertEquals(listOf("raichu", "jolteon"), result.map { it.toName })
    }

    @Test
    fun `query matches either side of the evolution`() {
        assertEquals(1, EvolutionFinder.filter(steps, null, null, "charmander").size)
        assertEquals(2, EvolutionFinder.filter(steps, null, null, "eevee").size)
    }

    @Test
    fun `method filter and item list stay consistent`() {
        assertEquals(listOf("thunder-stone", "water-stone"), EvolutionFinder.items(steps, EvoMethod.ITEM))
        assertEquals(emptyList<String>(), EvolutionFinder.items(steps, EvoMethod.LEVEL))
        assertEquals(listOf(EvoMethod.ITEM, EvoMethod.LEVEL), EvolutionFinder.methods(steps))
    }
}
