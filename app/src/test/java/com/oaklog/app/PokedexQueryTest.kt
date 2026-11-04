package com.oaklog.app

import com.oaklog.app.domain.logic.CaughtFilter
import com.oaklog.app.domain.logic.PokedexFilters
import com.oaklog.app.domain.logic.PokedexQuery
import com.oaklog.app.domain.logic.SortField
import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.CollectionStatus
import com.oaklog.app.domain.model.PokemonSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PokedexQueryTest {

    private val sampleData = listOf(
        PokemonSummary(
            id = 1,
            name = "bulbasaur",
            types = listOf("grass", "poison"),
            generation = 1,
            height = 7,
            weight = 69,
            baseStatTotal = 318,
            isLegendary = false,
            isMythical = false,
            isBaby = false,
        ),
        PokemonSummary(
            id = 4,
            name = "charmander",
            types = listOf("fire"),
            generation = 1,
            height = 6,
            weight = 85,
            baseStatTotal = 309,
            isLegendary = false,
            isMythical = false,
            isBaby = false,
        ),
        PokemonSummary(
            id = 25,
            name = "pikachu",
            types = listOf("electric"),
            generation = 1,
            height = 4,
            weight = 60,
            baseStatTotal = 320,
            isLegendary = false,
            isMythical = false,
            isBaby = false,
        ),
        PokemonSummary(
            id = 150,
            name = "mewtwo",
            types = listOf("psychic"),
            generation = 1,
            height = 20,
            weight = 1220,
            baseStatTotal = 680,
            isLegendary = true,
            isMythical = false,
            isBaby = false,
        ),
        PokemonSummary(
            id = 151,
            name = "mew",
            types = listOf("psychic"),
            generation = 1,
            height = 4,
            weight = 40,
            baseStatTotal = 600,
            isLegendary = false,
            isMythical = true,
            isBaby = false,
        ),
        PokemonSummary(
            id = 252,
            name = "treecko",
            types = listOf("grass"),
            generation = 3,
            height = 5,
            weight = 50,
            baseStatTotal = 310,
            isLegendary = false,
            isMythical = false,
            isBaby = false,
        ),
    )

    private val sampleCollection = mapOf(
        25 to CollectionEntry(speciesId = 25, status = CollectionStatus.CAUGHT, favorite = true),
        150 to CollectionEntry(speciesId = 150, status = CollectionStatus.SEEN, favorite = false),
    )

    @Test
    fun querySearchByName() {
        val result = PokedexQuery.apply(sampleData, sampleCollection, PokedexFilters(query = "pika"))
        assertEquals(1, result.size)
        assertEquals("pikachu", result.first().name)
    }

    @Test
    fun querySearchByNumber() {
        val result = PokedexQuery.apply(sampleData, sampleCollection, PokedexFilters(query = "#0150"))
        assertEquals(1, result.size)
        assertEquals("mewtwo", result.first().name)
    }

    @Test
    fun filterByGeneration() {
        val result = PokedexQuery.apply(sampleData, sampleCollection, PokedexFilters(generations = setOf(3)))
        assertEquals(1, result.size)
        assertEquals("treecko", result.first().name)
    }

    @Test
    fun filterByType() {
        val result = PokedexQuery.apply(sampleData, sampleCollection, PokedexFilters(types = setOf("psychic")))
        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "mewtwo" })
        assertTrue(result.any { it.name == "mew" })
    }

    @Test
    fun filterByLegendary() {
        val result = PokedexQuery.apply(sampleData, sampleCollection, PokedexFilters(legendaryOnly = true))
        assertEquals(1, result.size)
        assertEquals("mewtwo", result.first().name)
    }

    @Test
    fun filterByCaughtStatus() {
        val caughtOnly = PokedexQuery.apply(sampleData, sampleCollection, PokedexFilters(caught = CaughtFilter.CAUGHT))
        assertEquals(1, caughtOnly.size)
        assertEquals("pikachu", caughtOnly.first().name)

        val uncaughtOnly = PokedexQuery.apply(sampleData, sampleCollection, PokedexFilters(caught = CaughtFilter.UNCAUGHT))
        assertEquals(5, uncaughtOnly.size)
    }

    @Test
    fun filterByFavorites() {
        val favs = PokedexQuery.apply(sampleData, sampleCollection, PokedexFilters(favoritesOnly = true))
        assertEquals(1, favs.size)
        assertEquals("pikachu", favs.first().name)
    }

    @Test
    fun sortByStatTotalDescending() {
        val sorted = PokedexQuery.apply(
            sampleData,
            sampleCollection,
            PokedexFilters(sortField = SortField.STAT_TOTAL, sortDescending = true)
        )
        assertEquals("mewtwo", sorted.first().name)
        assertEquals(680, sorted.first().baseStatTotal)
    }
}

