package com.oaklog.app

import com.oaklog.app.data.remote.dto.AreaPokemonEncounterDto
import com.oaklog.app.data.remote.dto.EncounterDetailDto
import com.oaklog.app.data.remote.dto.EncounterVersionDetailDto
import com.oaklog.app.data.remote.dto.NamedRefDto
import com.oaklog.app.data.repository.AreaMapper
import com.oaklog.app.domain.logic.Places
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlacesTest {

    @Test
    fun `location labels drop the region prefix`() {
        assertEquals("Route 2", Places.locationLabel("kanto-route-2", "kanto"))
        assertEquals("Viridian Forest", Places.locationLabel("viridian-forest", "kanto"))
    }

    @Test
    fun `area labels keep only what tells areas apart`() {
        assertEquals("Main area", Places.areaLabel("kanto-route-2-area", "kanto-route-2"))
        assertEquals("South Towards Viridian City", Places.areaLabel("kanto-route-2-south-towards-viridian-city", "kanto-route-2"))
        assertEquals("Main area", Places.areaLabel("kanto-route-2", "kanto-route-2"))
    }

    @Test
    fun `level ranges read naturally`() {
        assertEquals("Lv 5", Places.levelRange(5, 5))
        assertEquals("Lv 3–7", Places.levelRange(3, 7))
        assertEquals("—", Places.levelRange(0, 0))
    }

    @Test
    fun `slots fold into one entry per game with the widest levels`() {
        val encounter = AreaPokemonEncounterDto(
            pokemon = NamedRefDto("rattata", "https://pokeapi.co/api/v2/pokemon/19/"),
            versionDetails = listOf(
                EncounterVersionDetailDto(
                    maxChance = 45,
                    version = NamedRefDto("red"),
                    encounterDetails = listOf(
                        EncounterDetailDto(chance = 25, minLevel = 3, maxLevel = 4, method = NamedRefDto("walk")),
                        EncounterDetailDto(chance = 20, minLevel = 2, maxLevel = 5, method = NamedRefDto("walk")),
                    ),
                ),
            ),
        )
        val spawn = AreaMapper.spawns(listOf(encounter)).single()
        val entry = spawn.entries.single()
        assertEquals(19, spawn.speciesId)
        assertEquals(2, entry.minLevel)
        assertEquals(5, entry.maxLevel)
        assertEquals(45, entry.chance)
        assertEquals(listOf("walk"), entry.methods)
    }

    @Test
    fun `alternate forms get no species id`() {
        val form = AreaPokemonEncounterDto(pokemon = NamedRefDto("rattata-alola", "https://pokeapi.co/api/v2/pokemon/10091/"))
        assertNull(AreaMapper.spawns(listOf(form)).single().speciesId)
    }
}
