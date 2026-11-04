package com.oaklog.app.data.repository

import com.oaklog.app.data.remote.dto.AreaPokemonEncounterDto
import com.oaklog.app.data.remote.dto.EncounterDetailDto
import com.oaklog.app.domain.model.AreaSpawn
import com.oaklog.app.domain.model.EncounterSlot
import com.oaklog.app.domain.model.SpawnEntry

/** Folds PokéAPI's per-slot encounter rows into one readable entry per Pokémon and game. */
internal object AreaMapper {

    /** PokéAPI numbers alternate forms from 10001, so anything below is a species. */
    private const val MAX_SPECIES_ID = 9999

    /** Details sharing a method and the same conditions are one slot, so their rates add up. */
    fun slots(details: List<EncounterDetailDto>): List<EncounterSlot> =
        details
            .groupBy { d -> d.method.name to d.conditionValues.map { it.name }.sorted() }
            .map { (key, group) ->
                EncounterSlot(
                    method = key.first,
                    chance = group.sumOf { it.chance }.coerceAtMost(100),
                    minLevel = group.minOf { it.minLevel },
                    maxLevel = group.maxOf { it.maxLevel },
                    conditions = key.second,
                )
            }
            .sortedWith(compareByDescending<EncounterSlot> { it.chance }.thenBy { it.method })

    fun spawns(encounters: List<AreaPokemonEncounterDto>): List<AreaSpawn> =
        encounters.map { e ->
            AreaSpawn(
                pokemonName = e.pokemon.name,
                speciesId = e.pokemon.id?.takeIf { it <= MAX_SPECIES_ID },
                entries = e.versionDetails.map { v ->
                    val details = v.encounterDetails
                    SpawnEntry(
                        version = v.version.name,
                        minLevel = details.minOfOrNull { it.minLevel } ?: 0,
                        maxLevel = details.maxOfOrNull { it.maxLevel } ?: 0,
                        chance = v.maxChance,
                        methods = details.map { it.method.name }.distinct(),
                        slots = slots(details),
                    )
                },
            )
        }.sortedBy { it.pokemonName }
}
