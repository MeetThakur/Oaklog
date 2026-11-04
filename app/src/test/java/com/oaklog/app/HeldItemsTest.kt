package com.oaklog.app

import com.oaklog.app.data.remote.dto.HeldVersionDto
import com.oaklog.app.data.remote.dto.ItemHeldByDto
import com.oaklog.app.data.remote.dto.NamedRefDto
import com.oaklog.app.data.remote.dto.PokemonHeldItemDto
import com.oaklog.app.data.repository.HeldMapper
import com.oaklog.app.domain.logic.HeldItems
import com.oaklog.app.domain.model.HeldItem
import com.oaklog.app.domain.model.HeldRarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HeldItemsTest {
    private val pikachu = listOf(
        HeldItem("oran-berry", listOf(HeldRarity("red", 50), HeldRarity("gold", 5))),
        HeldItem("light-ball", listOf(HeldRarity("gold", 5))),
    )

    @Test
    fun `rarity label is a single value or a span`() {
        assertEquals("50%", HeldItems.rarityLabel(listOf(HeldRarity("red", 50)), null))
        assertEquals("5–50%", HeldItems.rarityLabel(pikachu[0].entries, null))
        assertEquals("5%", HeldItems.rarityLabel(pikachu[0].entries, "gold"))
        assertNull(HeldItems.rarityLabel(pikachu[0].entries, "ruby"))
    }

    @Test
    fun `items are filtered to the selected game`() {
        assertEquals(listOf("oran-berry"), HeldItems.forGame(pikachu, "red").map { it.item })
        assertEquals(2, HeldItems.forGame(pikachu, null).size)
        assertEquals(listOf("gold", "red"), HeldItems.games(pikachu))
    }

    @Test
    fun `mapping sorts items and drops species ids of alternate forms`() {
        val items = HeldMapper.heldItems(
            listOf(
                PokemonHeldItemDto(NamedRefDto("zinc"), listOf(HeldVersionDto(5, NamedRefDto("red")))),
                PokemonHeldItemDto(NamedRefDto("apple"), listOf(HeldVersionDto(50, NamedRefDto("red")))),
            ),
        )
        assertEquals(listOf("apple", "zinc"), items.map { it.item })

        val by = HeldMapper.heldBy(
            listOf(
                ItemHeldByDto(NamedRefDto("pikachu-alola", "https://pokeapi.co/api/v2/pokemon/10094/")),
                ItemHeldByDto(NamedRefDto("pikachu", "https://pokeapi.co/api/v2/pokemon/25/")),
            ),
        )
        assertEquals(listOf("pikachu", "pikachu-alola"), by.map { it.pokemonName })
        assertNull(by.last().speciesId)
    }
}
