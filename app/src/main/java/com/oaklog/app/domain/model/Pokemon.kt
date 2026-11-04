package com.oaklog.app.domain.model

/** Lightweight species row used by the Pokédex list. Detail fields are null until hydrated. */
data class PokemonSummary(
    val id: Int,
    val name: String,
    val types: List<String>,
    val generation: Int?,
    val height: Int?,
    val weight: Int?,
    val baseStatTotal: Int?,
    val isLegendary: Boolean?,
    val isMythical: Boolean?,
    val isBaby: Boolean?,
) {
    val hasDetails: Boolean get() = baseStatTotal != null && isLegendary != null
}

enum class CollectionStatus { NOT_CAUGHT, SEEN, CAUGHT }

data class CollectionEntry(
    val speciesId: Int,
    val status: CollectionStatus = CollectionStatus.NOT_CAUGHT,
    val shiny: Boolean = false,
    val favorite: Boolean = false,
) {
    val isCaught: Boolean get() = status == CollectionStatus.CAUGHT
}

data class Stat(val key: String, val label: String, val value: Int)

data class AbilityRef(val name: String, val isHidden: Boolean)

/** One selectable form of a species (default, mega, regional, gmax, ...). */
data class PokemonVariety(val pokemonId: Int, val pokemonName: String, val isDefault: Boolean)

data class PokedexEntryText(val versionName: String, val text: String)

data class PokemonDetail(
    val id: Int,
    val speciesId: Int,
    val name: String,
    val displayName: String,
    val types: List<String>,
    val genus: String?,
    val heightDm: Int,
    val weightHg: Int,
    val abilities: List<AbilityRef>,
    val stats: List<Stat>,
    val baseExperience: Int?,
    val captureRate: Int?,
    /** Female share in eighths, or null for genderless. */
    val femaleEighths: Int?,
    val eggGroups: List<String>,
    val generation: Int?,
    val isLegendary: Boolean,
    val isMythical: Boolean,
    val isBaby: Boolean,
    val artworkUrl: String?,
    val shinyArtworkUrl: String?,
    val spriteUrl: String?,
    val varieties: List<PokemonVariety>,
    val dexEntries: List<PokedexEntryText>,
    val evolutionChainId: Int?,
    /** Audio clip URL (Ogg), or null when PokéAPI has none for this form. */
    val criesUrl: String? = null,
    /** Present only when the female looks different from the male. */
    val femaleSpriteUrl: String? = null,
    val shinyFemaleSpriteUrl: String? = null,
) {
    fun baseStat(key: String): Int = stats.firstOrNull { it.key == key }?.value ?: 0
    val baseStatTotal: Int get() = stats.sumOf { it.value }
}

// ---- Evolution -------------------------------------------------------------------------------

data class EvolutionNode(
    val speciesId: Int,
    val name: String,
    val isBaby: Boolean,
    /** One entry per alternative way to reach this node from its parent (usually zero or one). */
    val requirements: List<String>,
    val children: List<EvolutionNode>,
)

/** How a species evolves, reduced to the handful of kinds a person actually filters by. */
enum class EvoMethod(val label: String) {
    ITEM("Use an item"),
    LEVEL("Level up"),
    TRADE("Trade"),
    HAPPINESS("Friendship"),
    HELD_ITEM("Held item"),
    MOVE("Knows a move"),
    LOCATION("Location"),
    OTHER("Other"),
}

/** One evolution: [fromName] becomes [toName] by [method]. A species with two routes yields two steps. */
data class EvolutionStep(
    val fromId: Int,
    val fromName: String,
    val toId: Int,
    val toName: String,
    val method: EvoMethod,
    /** The stone or held item involved, as an API slug, when there is one. */
    val item: String?,
    /** Full requirement in words, e.g. "Level 36, Night". */
    val requirement: String,
)

// ---- Moves -----------------------------------------------------------------------------------

data class MoveInfo(
    val id: Int,
    val name: String,
    val type: String?,
    val damageClass: String?,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    val priority: Int,
    val description: String?,
    val generation: Int?,
    /** Species that can learn this move (default forms only), for the full move entry. */
    val learnedBy: List<NamedEntry> = emptyList(),
)

data class LearnEntry(
    val moveName: String,
    val method: String,
    val level: Int,
    val versionGroup: String,
)

// ---- Reference data --------------------------------------------------------------------------

data class NamedEntry(val id: Int, val name: String)

data class AbilityInfo(
    val id: Int,
    val name: String,
    val description: String?,
    val generation: Int?,
    val pokemon: List<AbilityPokemon>,
    /** One-sentence effect, suited to inline display next to a species' ability. */
    val shortDescription: String? = null,
)

data class AbilityPokemon(val pokemonName: String, val speciesId: Int?, val isHidden: Boolean)

data class ItemInfo(
    val id: Int,
    val name: String,
    val category: String?,
    val cost: Int?,
    val description: String?,
    val spriteUrl: String?,
    val attributes: List<String>,
)

data class GenerationInfo(val id: Int, val name: String, val region: String?, val versionGroups: List<String>)

data class RegionInfo(val id: Int, val name: String, val pokedexes: List<String>, val generation: Int?)

data class PokedexInfo(
    val name: String,
    val region: String?,
    val description: String?,
    /** Species id -> entry number in this dex. */
    val entries: Map<Int, Int>,
)

data class GameVersion(val name: String, val versionGroup: String, val generation: Int?)

data class VersionGroupInfo(val name: String, val order: Int, val generation: Int?, val versions: List<String>)

// ---- Encounters ------------------------------------------------------------------------------

data class EncounterSpot(
    val location: String,
    val method: String,
    val minLevel: Int,
    val maxLevel: Int,
    val chance: Int,
    val conditions: List<String>,
)

data class EncounterGroup(val version: String, val spots: List<EncounterSpot>)

// ---- Places ----------------------------------------------------------------------------------

/** How one game offers a Pokémon in an area. */
data class SpawnEntry(
    val version: String,
    val minLevel: Int,
    val maxLevel: Int,
    /** Highest chance across the area's encounter slots, in percent. */
    val chance: Int,
    val methods: List<String>,
)

/** A Pokémon seen somewhere in a location. [id] is null for alternate forms, which have no art here. */
data class SummaryMon(val name: String, val id: Int?)

/** Pokémon and level span for one game (or all games together). Levels are 0 when not recorded. */
data class GameSummary(val pokemon: List<SummaryMon>, val minLevel: Int, val maxLevel: Int)

/** What a location offers at a glance: across all games, plus the same broken down per game. */
data class LocationSummary(
    val pokemon: List<SummaryMon>,
    val minLevel: Int,
    val maxLevel: Int,
    val perGame: Map<String, GameSummary> = emptyMap(),
) {
    /** The summary for [game], or for all games when null. Null when the game has nothing here. */
    fun forGame(game: String?): GameSummary? =
        if (game == null) GameSummary(pokemon, minLevel, maxLevel) else perGame[game]
}

/** A wild Pokémon found in an area, with one entry per game that has it there. */
data class AreaSpawn(
    val pokemonName: String,
    /** Null for alternate forms, which have no detail page of their own. */
    val speciesId: Int?,
    val entries: List<SpawnEntry>,
)
