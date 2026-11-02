package com.oaklog.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Raw API payload cache, keyed by resource path (e.g. "pokemon/25"). */
@Entity(tableName = "cached_resource")
data class CachedResourceEntity(
    @PrimaryKey val key: String,
    val json: String,
    val fetchedAt: Long,
)

/**
 * One row per species. Type/generation come from cheap list endpoints at index sync time;
 * the nullable detail columns are filled in once the species + default Pokémon are fetched.
 */
@Entity(tableName = "pokemon_index")
data class PokemonIndexEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type1: String? = null,
    val type2: String? = null,
    val generation: Int? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val baseStatTotal: Int? = null,
    val isLegendary: Boolean? = null,
    val isMythical: Boolean? = null,
    val isBaby: Boolean? = null,
    val displayName: String? = null,
)

@Entity(tableName = "collection")
data class CollectionEntity(
    @PrimaryKey val speciesId: Int,
    /** NOT_CAUGHT, SEEN or CAUGHT. */
    val status: String = "NOT_CAUGHT",
    val shiny: Boolean = false,
    val favorite: Boolean = false,
)

@Entity(tableName = "team")
data class TeamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "team_member",
    foreignKeys = [ForeignKey(TeamEntity::class, ["id"], ["teamId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("teamId")],
)
data class TeamMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamId: Long,
    val position: Int,
    val speciesId: Int,
    /** Pokémon (variety) id actually used, may differ from species id for alternate forms. */
    val pokemonId: Int,
    val pokemonName: String,
    val nickname: String? = null,
    val ability: String? = null,
    /** Comma-separated move slugs, at most four. */
    val moves: String = "",
)
