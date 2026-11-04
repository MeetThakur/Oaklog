package com.oaklog.app.data.repository

import com.oaklog.app.data.remote.dto.AreaPokemonEncounterDto
import com.oaklog.app.domain.model.AreaSpawn
import com.oaklog.app.domain.model.SpawnEntry

/** Folds PokéAPI's per-slot encounter rows into one readable entry per Pokémon and game. */
internal object AreaMapper {

    /** PokéAPI numbers alternate forms from 10001, so anything below is a species. */
    private const val MAX_SPECIES_ID = 9999

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
                    )
                },
            )
        }.sortedBy { it.pokemonName }
}
