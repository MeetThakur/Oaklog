package com.oaklog.app.domain.repository

import com.oaklog.app.domain.logic.TypeChart
import com.oaklog.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Progress of the background pass that fills height, weight, BST and rarity flags into the index. */
data class IndexProgress(val done: Int, val total: Int)

interface PokemonRepository {
    fun observeIndex(): Flow<List<PokemonSummary>>
    suspend fun syncIndex(force: Boolean = false): Result<Unit>

    /** Non-null only while [hydrateIndex] is running. */
    val hydrationProgress: StateFlow<IndexProgress?>

    /**
     * Fills detail columns for every row that lacks them. Safe to call repeatedly: concurrent calls
     * collapse into one, and rows that fail are retried on the next call.
     */
    suspend fun hydrateIndex()
    suspend fun getPokemonDetail(speciesIdOrName: String, varietyName: String? = null): Result<PokemonDetail>
    suspend fun getLearnset(pokemonName: String): Result<List<LearnEntry>>
    suspend fun getEvolutionChain(chainId: Int): Result<EvolutionNode>
    suspend fun getMove(nameOrId: String): Result<MoveInfo>
    suspend fun getAbility(nameOrId: String): Result<AbilityInfo>
    suspend fun getItem(nameOrId: String): Result<ItemInfo>
    suspend fun getEncounters(pokemonIdOrName: String): Result<List<EncounterGroup>>
    suspend fun getPokedex(slug: String): Result<PokedexInfo>
    suspend fun getPokedexList(): Result<List<NamedEntry>>
    suspend fun getRegions(): Result<List<RegionInfo>>
    suspend fun getGenerations(): Result<List<GenerationInfo>>
    suspend fun getGameVersions(): Result<List<GameVersion>>
    suspend fun getTypeChart(): Result<TypeChart>
    fun observeCacheSize(): Flow<Pair<Int, Long>>
    suspend fun clearCache()
    suspend fun preloadGeneration(gen: Int, onProgress: (done: Int, total: Int) -> Unit): Result<Unit>
    suspend fun getMoveList(limit: Int = 100, offset: Int = 0): Result<List<NamedEntry>>
    suspend fun getAbilityList(limit: Int = 100, offset: Int = 0): Result<List<NamedEntry>>
    suspend fun getItemList(limit: Int = 100, offset: Int = 0): Result<List<NamedEntry>>
}

