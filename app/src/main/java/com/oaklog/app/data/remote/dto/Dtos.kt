package com.oaklog.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire models for PokéAPI v2. Everything the API may omit is nullable or defaulted; only fields
 * OakLog reads are declared (unknown keys are ignored by the shared Json instance).
 */

@Serializable
data class NamedRefDto(val name: String = "", val url: String = "") {
    /** Numeric id parsed from the trailing path segment of [url], or null when absent. */
    val id: Int? get() = url.trimEnd('/').substringAfterLast('/').toIntOrNull()
}

@Serializable
data class ResourceListDto(
    val count: Int = 0,
    val next: String? = null,
    val results: List<NamedRefDto> = emptyList(),
)

@Serializable
data class LanguageTextDto(
    @SerialName("flavor_text") val flavorText: String? = null,
    val text: String? = null,
    val effect: String? = null,
    @SerialName("short_effect") val shortEffect: String? = null,
    val genus: String? = null,
    val language: NamedRefDto? = null,
    val version: NamedRefDto? = null,
    @SerialName("version_group") val versionGroup: NamedRefDto? = null,
)

// ---- Pokémon ---------------------------------------------------------------------------------

@Serializable
data class PokemonDto(
    val id: Int,
    val name: String,
    val height: Int = 0,
    val weight: Int = 0,
    @SerialName("base_experience") val baseExperience: Int? = null,
    @SerialName("is_default") val isDefault: Boolean = true,
    val types: List<PokemonTypeSlotDto> = emptyList(),
    val stats: List<PokemonStatDto> = emptyList(),
    val abilities: List<PokemonAbilitySlotDto> = emptyList(),
    val moves: List<PokemonMoveDto> = emptyList(),
    val sprites: SpritesDto = SpritesDto(),
    val species: NamedRefDto? = null,
    val cries: CriesDto? = null,
    @SerialName("held_items") val heldItems: List<PokemonHeldItemDto> = emptyList(),
)

@Serializable
data class PokemonHeldItemDto(
    val item: NamedRefDto,
    @SerialName("version_details") val versionDetails: List<HeldVersionDto> = emptyList(),
)

@Serializable
data class HeldVersionDto(val rarity: Int = 0, val version: NamedRefDto)

@Serializable
data class ItemHeldByDto(
    val pokemon: NamedRefDto,
    @SerialName("version_details") val versionDetails: List<HeldVersionDto> = emptyList(),
)

@Serializable
data class CriesDto(val latest: String? = null, val legacy: String? = null)

@Serializable
data class PokemonTypeSlotDto(val slot: Int = 0, val type: NamedRefDto)

@Serializable
data class PokemonStatDto(@SerialName("base_stat") val baseStat: Int = 0, val stat: NamedRefDto)

@Serializable
data class PokemonAbilitySlotDto(
    @SerialName("is_hidden") val isHidden: Boolean = false,
    val slot: Int = 0,
    val ability: NamedRefDto,
)

@Serializable
data class PokemonMoveDto(
    val move: NamedRefDto,
    @SerialName("version_group_details") val versionGroupDetails: List<MoveVersionDetailDto> = emptyList(),
)

@Serializable
data class MoveVersionDetailDto(
    @SerialName("level_learned_at") val levelLearnedAt: Int = 0,
    @SerialName("move_learn_method") val moveLearnMethod: NamedRefDto,
    @SerialName("version_group") val versionGroup: NamedRefDto,
)

@Serializable
data class SpritesDto(
    @SerialName("front_default") val frontDefault: String? = null,
    @SerialName("front_shiny") val frontShiny: String? = null,
    @SerialName("front_female") val frontFemale: String? = null,
    @SerialName("front_shiny_female") val frontShinyFemale: String? = null,
    val other: OtherSpritesDto? = null,
)

@Serializable
data class OtherSpritesDto(
    @SerialName("official-artwork") val officialArtwork: ArtworkDto? = null,
    val home: ArtworkDto? = null,
)

@Serializable
data class ArtworkDto(
    @SerialName("front_default") val frontDefault: String? = null,
    @SerialName("front_shiny") val frontShiny: String? = null,
)

// ---- Species ---------------------------------------------------------------------------------

@Serializable
data class SpeciesDto(
    val id: Int,
    val name: String,
    @SerialName("is_baby") val isBaby: Boolean = false,
    @SerialName("is_legendary") val isLegendary: Boolean = false,
    @SerialName("is_mythical") val isMythical: Boolean = false,
    @SerialName("gender_rate") val genderRate: Int = -1,
    @SerialName("capture_rate") val captureRate: Int? = null,
    @SerialName("growth_rate") val growthRate: NamedRefDto? = null,
    @SerialName("base_happiness") val baseHappiness: Int? = null,
    @SerialName("hatch_counter") val hatchCounter: Int? = null,
    @SerialName("egg_groups") val eggGroups: List<NamedRefDto> = emptyList(),
    @SerialName("evolution_chain") val evolutionChain: UrlRefDto? = null,
    @SerialName("evolves_from_species") val evolvesFromSpecies: NamedRefDto? = null,
    val generation: NamedRefDto? = null,
    val genera: List<LanguageTextDto> = emptyList(),
    val names: List<SpeciesNameDto> = emptyList(),
    @SerialName("flavor_text_entries") val flavorTextEntries: List<LanguageTextDto> = emptyList(),
    val varieties: List<SpeciesVarietyDto> = emptyList(),
    @SerialName("pokedex_numbers") val pokedexNumbers: List<PokedexNumberDto> = emptyList(),
)

@Serializable
data class UrlRefDto(val url: String = "")

@Serializable
data class SpeciesNameDto(val name: String = "", val language: NamedRefDto? = null)

@Serializable
data class SpeciesVarietyDto(
    @SerialName("is_default") val isDefault: Boolean = false,
    val pokemon: NamedRefDto,
)

@Serializable
data class PokedexNumberDto(@SerialName("entry_number") val entryNumber: Int = 0, val pokedex: NamedRefDto)

// ---- Evolution -------------------------------------------------------------------------------

@Serializable
data class EvolutionChainDto(val id: Int = 0, val chain: ChainLinkDto)

@Serializable
data class ChainLinkDto(
    @SerialName("is_baby") val isBaby: Boolean = false,
    val species: NamedRefDto,
    @SerialName("evolution_details") val evolutionDetails: List<EvolutionDetailDto> = emptyList(),
    @SerialName("evolves_to") val evolvesTo: List<ChainLinkDto> = emptyList(),
)

@Serializable
data class EvolutionDetailDto(
    val item: NamedRefDto? = null,
    val trigger: NamedRefDto? = null,
    val gender: Int? = null,
    @SerialName("held_item") val heldItem: NamedRefDto? = null,
    @SerialName("known_move") val knownMove: NamedRefDto? = null,
    @SerialName("known_move_type") val knownMoveType: NamedRefDto? = null,
    val location: NamedRefDto? = null,
    @SerialName("min_level") val minLevel: Int? = null,
    @SerialName("min_happiness") val minHappiness: Int? = null,
    @SerialName("min_beauty") val minBeauty: Int? = null,
    @SerialName("min_affection") val minAffection: Int? = null,
    @SerialName("needs_overworld_rain") val needsOverworldRain: Boolean = false,
    @SerialName("party_species") val partySpecies: NamedRefDto? = null,
    @SerialName("party_type") val partyType: NamedRefDto? = null,
    @SerialName("relative_physical_stats") val relativePhysicalStats: Int? = null,
    @SerialName("time_of_day") val timeOfDay: String? = null,
    @SerialName("trade_species") val tradeSpecies: NamedRefDto? = null,
    @SerialName("turn_upside_down") val turnUpsideDown: Boolean = false,
)

// ---- Types, moves, abilities, items ----------------------------------------------------------

@Serializable
data class TypeDto(
    val id: Int,
    val name: String,
    @SerialName("damage_relations") val damageRelations: DamageRelationsDto = DamageRelationsDto(),
    val pokemon: List<TypePokemonDto> = emptyList(),
)

@Serializable
data class DamageRelationsDto(
    @SerialName("double_damage_from") val doubleDamageFrom: List<NamedRefDto> = emptyList(),
    @SerialName("double_damage_to") val doubleDamageTo: List<NamedRefDto> = emptyList(),
    @SerialName("half_damage_from") val halfDamageFrom: List<NamedRefDto> = emptyList(),
    @SerialName("half_damage_to") val halfDamageTo: List<NamedRefDto> = emptyList(),
    @SerialName("no_damage_from") val noDamageFrom: List<NamedRefDto> = emptyList(),
    @SerialName("no_damage_to") val noDamageTo: List<NamedRefDto> = emptyList(),
)

@Serializable
data class TypePokemonDto(val slot: Int = 0, val pokemon: NamedRefDto)

@Serializable
data class MoveDto(
    val id: Int,
    val name: String,
    val accuracy: Int? = null,
    val power: Int? = null,
    val pp: Int? = null,
    val priority: Int = 0,
    @SerialName("damage_class") val damageClass: NamedRefDto? = null,
    val type: NamedRefDto? = null,
    @SerialName("effect_chance") val effectChance: Int? = null,
    @SerialName("effect_entries") val effectEntries: List<LanguageTextDto> = emptyList(),
    @SerialName("flavor_text_entries") val flavorTextEntries: List<LanguageTextDto> = emptyList(),
    val generation: NamedRefDto? = null,
    @SerialName("learned_by_pokemon") val learnedByPokemon: List<NamedRefDto> = emptyList(),
)

@Serializable
data class AbilityDto(
    val id: Int,
    val name: String,
    @SerialName("is_main_series") val isMainSeries: Boolean = true,
    val generation: NamedRefDto? = null,
    @SerialName("effect_entries") val effectEntries: List<LanguageTextDto> = emptyList(),
    @SerialName("flavor_text_entries") val flavorTextEntries: List<LanguageTextDto> = emptyList(),
    val pokemon: List<AbilityPokemonDto> = emptyList(),
)

@Serializable
data class AbilityPokemonDto(
    @SerialName("is_hidden") val isHidden: Boolean = false,
    val slot: Int = 0,
    val pokemon: NamedRefDto,
)

@Serializable
data class ItemDto(
    val id: Int,
    val name: String,
    val cost: Int? = null,
    @SerialName("fling_power") val flingPower: Int? = null,
    val category: NamedRefDto? = null,
    val attributes: List<NamedRefDto> = emptyList(),
    @SerialName("effect_entries") val effectEntries: List<LanguageTextDto> = emptyList(),
    @SerialName("flavor_text_entries") val flavorTextEntries: List<LanguageTextDto> = emptyList(),
    val sprites: ItemSpritesDto = ItemSpritesDto(),
    @SerialName("held_by_pokemon") val heldByPokemon: List<ItemHeldByDto> = emptyList(),
)

@Serializable
data class ItemSpritesDto(val default: String? = null)

@Serializable
data class ItemCategoryDto(
    val id: Int,
    val name: String,
    val items: List<NamedRefDto> = emptyList(),
)

// ---- Berries ---------------------------------------------------------------------------------

@Serializable
data class BerryDto(
    val id: Int,
    val name: String,
    @SerialName("growth_time") val growthTime: Int = 0,
    @SerialName("max_harvest") val maxHarvest: Int = 0,
    @SerialName("natural_gift_power") val naturalGiftPower: Int = 0,
    val size: Int = 0,
    val smoothness: Int = 0,
    @SerialName("soil_dryness") val soilDryness: Int = 0,
    val firmness: NamedRefDto? = null,
    val flavors: List<BerryFlavorDto> = emptyList(),
    val item: NamedRefDto? = null,
    @SerialName("natural_gift_type") val naturalGiftType: NamedRefDto? = null,
)

@Serializable
data class BerryFlavorDto(val potency: Int = 0, val flavor: NamedRefDto)

// ---- Generations, regions, pokédexes, versions -----------------------------------------------

@Serializable
data class GenerationDto(
    val id: Int,
    val name: String,
    @SerialName("main_region") val mainRegion: NamedRefDto? = null,
    @SerialName("pokemon_species") val pokemonSpecies: List<NamedRefDto> = emptyList(),
    @SerialName("version_groups") val versionGroups: List<NamedRefDto> = emptyList(),
)

@Serializable
data class RegionDto(
    val id: Int,
    val name: String,
    @SerialName("main_generation") val mainGeneration: NamedRefDto? = null,
    val pokedexes: List<NamedRefDto> = emptyList(),
    val locations: List<NamedRefDto> = emptyList(),
    @SerialName("version_groups") val versionGroups: List<NamedRefDto> = emptyList(),
)

@Serializable
data class LocationDto(
    val id: Int,
    val name: String,
    val region: NamedRefDto? = null,
    val areas: List<NamedRefDto> = emptyList(),
)

@Serializable
data class LocationAreaDto(
    val id: Int,
    val name: String,
    @SerialName("pokemon_encounters") val pokemonEncounters: List<AreaPokemonEncounterDto> = emptyList(),
)

@Serializable
data class AreaPokemonEncounterDto(
    val pokemon: NamedRefDto,
    @SerialName("version_details") val versionDetails: List<EncounterVersionDetailDto> = emptyList(),
)

@Serializable
data class PokedexDto(
    val id: Int,
    val name: String,
    @SerialName("is_main_series") val isMainSeries: Boolean = true,
    val region: NamedRefDto? = null,
    val descriptions: List<PokedexDescriptionDto> = emptyList(),
    @SerialName("pokemon_entries") val pokemonEntries: List<PokedexEntryDto> = emptyList(),
    @SerialName("version_groups") val versionGroups: List<NamedRefDto> = emptyList(),
)

@Serializable
data class PokedexDescriptionDto(val description: String = "", val language: NamedRefDto? = null)

@Serializable
data class PokedexEntryDto(
    @SerialName("entry_number") val entryNumber: Int = 0,
    @SerialName("pokemon_species") val pokemonSpecies: NamedRefDto,
)

@Serializable
data class VersionDto(
    val id: Int,
    val name: String,
    @SerialName("version_group") val versionGroup: NamedRefDto,
    val names: List<SpeciesNameDto> = emptyList(),
)

@Serializable
data class VersionGroupDto(
    val id: Int,
    val name: String,
    val order: Int = 0,
    val generation: NamedRefDto? = null,
    val versions: List<NamedRefDto> = emptyList(),
    val regions: List<NamedRefDto> = emptyList(),
    val pokedexes: List<NamedRefDto> = emptyList(),
)

// ---- Encounters ------------------------------------------------------------------------------

@Serializable
data class LocationAreaEncounterDto(
    @SerialName("location_area") val locationArea: NamedRefDto,
    @SerialName("version_details") val versionDetails: List<EncounterVersionDetailDto> = emptyList(),
)

@Serializable
data class EncounterVersionDetailDto(
    @SerialName("max_chance") val maxChance: Int = 0,
    val version: NamedRefDto,
    @SerialName("encounter_details") val encounterDetails: List<EncounterDetailDto> = emptyList(),
)

@Serializable
data class EncounterDetailDto(
    val chance: Int = 0,
    @SerialName("min_level") val minLevel: Int = 0,
    @SerialName("max_level") val maxLevel: Int = 0,
    val method: NamedRefDto,
    @SerialName("condition_values") val conditionValues: List<NamedRefDto> = emptyList(),
)
