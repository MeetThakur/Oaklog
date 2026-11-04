package com.oaklog.app.data.repository

import com.oaklog.app.data.remote.dto.HeldVersionDto
import com.oaklog.app.data.remote.dto.ItemHeldByDto
import com.oaklog.app.data.remote.dto.PokemonHeldItemDto
import com.oaklog.app.domain.model.HeldBy
import com.oaklog.app.domain.model.HeldItem
import com.oaklog.app.domain.model.HeldRarity

internal object HeldMapper {
    private const val MAX_SPECIES_ID = 9999

    private fun rarities(details: List<HeldVersionDto>) =
        details.map { HeldRarity(it.version.name, it.rarity) }

    fun heldItems(dtos: List<PokemonHeldItemDto>): List<HeldItem> =
        dtos.map { HeldItem(it.item.name, rarities(it.versionDetails)) }.sortedBy { it.item }

    fun heldBy(dtos: List<ItemHeldByDto>): List<HeldBy> =
        dtos.map {
            HeldBy(
                pokemonName = it.pokemon.name,
                speciesId = it.pokemon.id?.takeIf { id -> id <= MAX_SPECIES_ID },
                entries = rarities(it.versionDetails),
            )
        }.sortedWith(compareBy({ it.speciesId ?: Int.MAX_VALUE }, { it.pokemonName }))
}
