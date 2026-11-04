package com.oaklog.app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.data.prefs.SettingsDataStore
import com.oaklog.app.domain.logic.Learnset
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
    val learnset: List<LearnEntry> = emptyList(),
    val moveDetails: Map<String, MoveInfo> = emptyMap(),
    val abilityInfo: Map<String, AbilityInfo> = emptyMap(),
    val collectionEntry: CollectionEntry? = null,
    val selectedVarietyName: String? = null,
    val isShiny: Boolean = false,
    val isFemale: Boolean = false,
    val selectedDexVersion: String? = null,
    val selectedEncounterVersion: String? = null,
    val selectedMoveVersionGroup: String? = null,
    val selectedMoveMethod: String = "level-up",
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

    /** Known from the route, so the header can show the artwork a list card is morphing from. */
    val initialSpeciesId: Int? = pokemonIdArg.toIntOrNull()

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

                    detail.abilities.forEach { ref ->
                        launch {
                            pokemonRepo.getAbility(ref.name).onSuccess { info ->
                                _uiState.update { it.copy(abilityInfo = it.abilityInfo + (ref.name to info)) }
                            }
                        }
                    }

                    launch {
                        pokemonRepo.getLearnset(detail.name).onSuccess { entries ->
                            _uiState.update {
                                it.copy(
                                    learnset = entries,
                                    selectedMoveVersionGroup = Learnset.versionGroups(entries).firstOrNull(),
                                    selectedMoveMethod = "level-up",
                                )
                            }
                        }
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

    fun toggleFemale() {
        _uiState.update { it.copy(isFemale = !it.isFemale) }
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

    fun selectMoveVersionGroup(group: String) {
        _uiState.update { state ->
            val methods = Learnset.methods(state.learnset, group)
            val method = state.selectedMoveMethod.takeIf { it in methods } ?: methods.firstOrNull() ?: "level-up"
            state.copy(selectedMoveVersionGroup = group, selectedMoveMethod = method)
        }
    }

    fun selectMoveMethod(method: String) {
        _uiState.update { it.copy(selectedMoveMethod = method) }
    }

    /** Fetches move stats the first time a row is expanded. */
    fun loadMove(name: String) {
        if (_uiState.value.moveDetails.containsKey(name)) return
        viewModelScope.launch(Dispatchers.IO) {
            pokemonRepo.getMove(name).onSuccess { info ->
                _uiState.update { it.copy(moveDetails = it.moveDetails + (name to info)) }
            }
        }
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

