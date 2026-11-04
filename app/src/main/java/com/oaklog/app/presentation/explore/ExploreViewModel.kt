package com.oaklog.app.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.domain.logic.TypeChart
import com.oaklog.app.domain.model.*
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreUiState(
    val typeChart: TypeChart? = null,
    val movesList: List<MoveInfo> = emptyList(),
    val abilitiesList: List<AbilityInfo> = emptyList(),
    val itemsList: List<ItemInfo> = emptyList(),
    val heldItemsList: List<ItemInfo> = emptyList(),
    val regions: List<RegionInfo> = emptyList(),
    val generations: List<GenerationInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val pokemonRepo: PokemonRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState(isLoading = true))
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }

            pokemonRepo.getTypeChart().onSuccess { chart ->
                _uiState.update { it.copy(typeChart = chart) }
            }

            pokemonRepo.getRegions().onSuccess { regs ->
                _uiState.update { it.copy(regions = regs) }
            }

            pokemonRepo.getGenerations().onSuccess { gens ->
                _uiState.update { it.copy(generations = gens) }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun loadInitialMoves() {
        if (_uiState.value.movesList.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            pokemonRepo.getMoveList(limit = 40).onSuccess { list ->
                val details = fetchDetails(list) { pokemonRepo.getMove(it).getOrNull() }
                _uiState.update { it.copy(movesList = details, isLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun searchMove(query: String) {
        if (query.isBlank()) {
            // Drop the last search result, or the "initial list" loader would see a non-empty list and skip.
            searchJobs["moves"]?.cancel()
            _uiState.update { it.copy(movesList = emptyList()) }
            loadInitialMoves()
            return
        }
        launchSearch("moves", query, { pokemonRepo.getMoveList(limit = 2000) }, { pokemonRepo.getMove(it).getOrNull() }) { found ->
            _uiState.update { it.copy(movesList = found, isLoading = false) }
        }
    }

    fun loadInitialAbilities() {
        if (_uiState.value.abilitiesList.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            pokemonRepo.getAbilityList(limit = 40).onSuccess { list ->
                val details = fetchDetails(list) { pokemonRepo.getAbility(it).getOrNull() }
                _uiState.update { it.copy(abilitiesList = details, isLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun searchAbility(query: String) {
        if (query.isBlank()) {
            // Drop the last search result, or the "initial list" loader would see a non-empty list and skip.
            searchJobs["abilities"]?.cancel()
            _uiState.update { it.copy(abilitiesList = emptyList()) }
            loadInitialAbilities()
            return
        }
        launchSearch("abilities", query, { pokemonRepo.getAbilityList(limit = 1000) }, { pokemonRepo.getAbility(it).getOrNull() }) { found ->
            _uiState.update { it.copy(abilitiesList = found, isLoading = false) }
        }
    }

    fun loadHeldItems() {
        if (_uiState.value.heldItemsList.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            pokemonRepo.getHeldItems()
                .onSuccess { list -> _uiState.update { it.copy(heldItemsList = list, isLoading = false) } }
                .onFailure { _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun loadInitialItems() {
        if (_uiState.value.itemsList.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            pokemonRepo.getItemList(limit = 40).onSuccess { list ->
                val details = fetchDetails(list) { pokemonRepo.getItem(it).getOrNull() }
                _uiState.update { it.copy(itemsList = details, isLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun searchItem(query: String) {
        if (query.isBlank()) {
            // Drop the last search result, or the "initial list" loader would see a non-empty list and skip.
            searchJobs["items"]?.cancel()
            _uiState.update { it.copy(itemsList = emptyList()) }
            loadInitialItems()
            return
        }
        launchSearch("items", query, { pokemonRepo.getItemList(limit = 3000) }, { pokemonRepo.getItem(it).getOrNull() }) { found ->
            _uiState.update { it.copy(itemsList = found, isLoading = false) }
        }
    }

    private val searchJobs = mutableMapOf<String, Job>()
    private val nameLists = mutableMapOf<String, List<NamedEntry>>()

    /**
     * Matches [query] against the cached name list (not an exact-slug lookup), then loads details for the
     * best few. Typing again cancels the previous search so a slow reply can't overwrite a newer one.
     */
    private fun <T : Any> launchSearch(
        kind: String,
        query: String,
        loadNames: suspend () -> Result<List<NamedEntry>>,
        load: suspend (String) -> T?,
        publish: (List<T>) -> Unit,
    ) {
        searchJobs[kind]?.cancel()
        searchJobs[kind] = viewModelScope.launch(Dispatchers.IO) {
            delay(250)
            _uiState.update { it.copy(isLoading = true) }
            val names = nameLists[kind] ?: loadNames().getOrNull()?.also { nameLists[kind] = it }
            if (names == null) {
                publish(emptyList())
                return@launch
            }
            val needle = query.trim().lowercase().replace(' ', '-')
            fun rank(name: String) = when {
                name == needle -> 0
                name.startsWith(needle) -> 1
                name.split('-').any { it.startsWith(needle) } -> 2
                name.contains(needle) -> 3
                else -> -1
            }
            val top = names.mapNotNull { e -> rank(e.name).takeIf { it >= 0 }?.let { it to e } }
                .sortedWith(compareBy({ it.first }, { it.second.name }))
                .take(20).map { it.second }
            publish(fetchDetails(top, load))
        }
    }

    /** Resolves details a few at a time so a cold cache doesn't serialize 40 network calls. */
    private suspend fun <T : Any> fetchDetails(
        entries: List<NamedEntry>,
        load: suspend (String) -> T?,
    ): List<T> = entries.chunked(8).flatMap { batch ->
        coroutineScope { batch.map { async { load(it.name) } }.awaitAll().filterNotNull() }
    }
}
