package com.oaklog.app.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.domain.logic.TypeChart
import com.oaklog.app.domain.model.*
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
        val q = query.trim().lowercase().replace(' ', '-')
        if (q.isEmpty()) {
            loadInitialMoves()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            pokemonRepo.getMove(q).onSuccess { move ->
                _uiState.update { it.copy(movesList = listOf(move), isLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(movesList = emptyList(), isLoading = false) }
            }
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
        val q = query.trim().lowercase().replace(' ', '-')
        if (q.isEmpty()) {
            loadInitialAbilities()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            pokemonRepo.getAbility(q).onSuccess { ability ->
                _uiState.update { it.copy(abilitiesList = listOf(ability), isLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(abilitiesList = emptyList(), isLoading = false) }
            }
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
        val q = query.trim().lowercase().replace(' ', '-')
        if (q.isEmpty()) {
            loadInitialItems()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            pokemonRepo.getItem(q).onSuccess { item ->
                _uiState.update { it.copy(itemsList = listOf(item), isLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(itemsList = emptyList(), isLoading = false) }
            }
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
