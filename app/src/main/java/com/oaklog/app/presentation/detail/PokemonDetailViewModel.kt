package com.oaklog.app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.data.prefs.SettingsDataStore
import com.oaklog.app.domain.model.*
import com.oaklog.app.domain.repository.CollectionRepository
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PokemonDetailUiState(
    val detail: PokemonDetail? = null,
    val evolutionRoot: EvolutionNode? = null,
    val encounters: List<EncounterGroup> = emptyList(),
    val moves: List<MoveInfo> = emptyList(),
    val collectionEntry: CollectionEntry? = null,
    val selectedVarietyName: String? = null,
    val isShiny: Boolean = false,
    val selectedDexVersion: String? = null,
    val selectedEncounterVersion: String? = null,
    val selectedMoveMethod: String = "all",
    val selectedMoveSort: String = "level",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val pokemonRepo: PokemonRepository,
    private val collectionRepo: CollectionRepository,
    private val settingsStore: SettingsDataStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val pokemonIdArg: String = checkNotNull(savedStateHandle["pokemonId"])

    private val _uiState = MutableStateFlow(PokemonDetailUiState(isLoading = true))
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail(pokemonIdArg)
        observeCollection()
    }

    private fun observeCollection() {
        val id = pokemonIdArg.toIntOrNull() ?: return
        viewModelScope.launch {
            collectionRepo.observeCollection().collect { collectionMap ->
                val entry = collectionMap[id]
                _uiState.update { it.copy(collectionEntry = entry) }
            }
        }
    }

    fun loadDetail(speciesIdOrName: String, varietyName: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null, selectedVarietyName = varietyName) }

            pokemonRepo.getPokemonDetail(speciesIdOrName, varietyName)
                .onSuccess { detail ->
                    val defaultDexVersion = detail.dexEntries.firstOrNull()?.versionName
                    _uiState.update {
                        it.copy(
                            detail = detail,
                            selectedDexVersion = defaultDexVersion,
                            isLoading = false,
                        )
                    }

                    // Fetch evolution chain if available
                    detail.evolutionChainId?.let { chainId ->
                        pokemonRepo.getEvolutionChain(chainId).onSuccess { node ->
                            _uiState.update { it.copy(evolutionRoot = node) }
                        }
                    }

                    // Fetch encounters
                    pokemonRepo.getEncounters(detail.name).onSuccess { encList ->
                        val initialVersion = encList.firstOrNull()?.version
                        _uiState.update {
                            it.copy(
                                encounters = encList,
                                selectedEncounterVersion = initialVersion,
                            )
                        }
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = err.localizedMessage ?: "Failed to load Pokémon details",
                        )
                    }
                }
        }
    }

    fun toggleShiny() {
        _uiState.update { it.copy(isShiny = !it.isShiny) }
    }

    fun selectVariety(varietyName: String) {
        val currentDetail = _uiState.value.detail ?: return
        loadDetail(currentDetail.speciesId.toString(), varietyName)
    }

    fun selectDexVersion(version: String) {
        _uiState.update { it.copy(selectedDexVersion = version) }
    }

    fun selectEncounterVersion(version: String) {
        _uiState.update { it.copy(selectedEncounterVersion = version) }
    }

    fun selectMoveMethod(method: String) {
        _uiState.update { it.copy(selectedMoveMethod = method) }
    }

    fun selectMoveSort(sort: String) {
        _uiState.update { it.copy(selectedMoveSort = sort) }
    }

    fun cycleCollectionStatus() {
        val id = _uiState.value.detail?.speciesId ?: return
        viewModelScope.launch {
            collectionRepo.cycleStatus(id)
        }
    }

    fun toggleFavorite() {
        val id = _uiState.value.detail?.speciesId ?: return
        viewModelScope.launch {
            collectionRepo.toggleFavorite(id)
        }
    }
}
