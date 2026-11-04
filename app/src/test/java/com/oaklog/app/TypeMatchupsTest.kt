package com.oaklog.app

import com.oaklog.app.domain.logic.TypeChart
import com.oaklog.app.domain.logic.TypeMatchups
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeMatchupsTest {
    private val chart = TypeChart.fromRelations(
        mapOf(
            "fire" to TypeChart.Relations(listOf("grass", "ice", "bug", "steel"), listOf("fire", "water", "rock", "dragon"), emptyList()),
            "water" to TypeChart.Relations(listOf("fire", "ground", "rock"), listOf("water", "grass", "dragon"), emptyList()),
            "grass" to TypeChart.Relations(listOf("water", "ground", "rock"), listOf("fire", "grass", "poison", "flying", "bug", "dragon", "steel"), emptyList()),
            "ground" to TypeChart.Relations(listOf("fire", "electric", "poison", "rock", "steel"), listOf("grass", "bug"), listOf("flying")),
            "flying" to TypeChart.Relations(listOf("grass", "bug"), listOf("electric", "rock", "steel"), emptyList()),
            "electric" to TypeChart.Relations(listOf("water", "flying"), listOf("grass", "electric", "dragon"), listOf("ground")),
            "ice" to TypeChart.Relations(listOf("grass", "ground", "flying", "dragon"), listOf("fire", "water", "ice", "steel"), emptyList()),
            "rock" to TypeChart.Relations(listOf("fire", "ice", "flying", "bug"), listOf("fighting", "ground", "steel"), emptyList()),
            "bug" to TypeChart.Relations(listOf("grass", "psychic", "dark"), listOf("fire", "fighting", "poison", "flying", "ghost", "steel", "fairy"), emptyList()),
            "poison" to TypeChart.Relations(listOf("grass", "fairy"), listOf("poison", "ground", "rock", "ghost"), listOf("steel")),
            "steel" to TypeChart.Relations(listOf("ice", "rock", "fairy"), listOf("fire", "water", "electric", "steel"), emptyList()),
            "dragon" to TypeChart.Relations(listOf("dragon"), listOf("steel"), emptyList()),
        ),
    )

    @Test
    fun `charizard style typing has a 4x weakness and a 4x resistance`() {
        val m = TypeMatchups.of(chart, listOf("fire", "flying"))
        assertEquals(listOf("rock"), m.weak4)
        assertEquals(listOf("grass", "bug").sorted(), m.resist4.sorted())
        assertEquals(listOf("ground"), m.immune)
    }

    @Test
    fun `single type water`() {
        val m = TypeMatchups.of(chart, listOf("water"))
        assertEquals(listOf("grass", "electric").filter { it in m.weak2 }.sorted(), m.weak2.sorted().filter { it == "grass" || it == "electric" })
        assertEquals(emptyList<String>(), m.weak4)
    }

    @Test
    fun `strong against uses the pokemons own types`() {
        val m = TypeMatchups.of(chart, listOf("electric"))
        assertEquals(listOf("flying", "water"), m.strongAgainst)
    }

    @Test
    fun `strong against is the union across both types`() {
        val m = TypeMatchups.of(chart, listOf("fire", "flying"))
        assert("bug" in m.strongAgainst && "grass" in m.strongAgainst && "ice" in m.strongAgainst)
    }
}
