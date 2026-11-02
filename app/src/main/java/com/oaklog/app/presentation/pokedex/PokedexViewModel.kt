package com.oaklog.app.presentation.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.data.prefs.SettingsDataStore
import com.oaklog.app.domain.logic.CaughtFilter
import com.oaklog.app.domain.logic.PokedexFilters
import com.oaklog.app.domain.logic.PokedexQuery
import com.oaklog.app.domain.logic.SortField
import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.DexLayout
import com.oaklog.app.domain.model.PokemonSummary
import com.oaklog.app.domain.model.UserSettings
import com.oaklog.app.domain.repository.CollectionRepository
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PokedexUiState(
    val pokemonList: List<PokemonSummary> = emptyList(),
    val collectionMap: Map<Int, CollectionEntry> = emptyMap(),
    val filters: PokedexFilters = PokedexFilters(),
    val layout: DexLayout = DexLayout.GRID,
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalCount: Int = 0,
)

@HiltViewModel
class PokedexViewModel @Inject constructor(
    private val pokemonRepo: PokemonRepository,
    private val collectionRepo: CollectionRepository,
    private val settingsStore: SettingsDataStore,
) : ViewModel() {

    private val _filters = MutableStateFlow(PokedexFilters())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PokedexUiState> = combine(
        pokemonRepo.observeIndex(),
        collectionRepo.observeCollection(),
        _filters,
        settingsStore.settings,
        combine(_isLoading, _error) { l, e -> l to e },
    ) { allPokemon, collection, filters, settings, status ->
        val filtered = PokedexQuery.apply(allPokemon, collection, filters)
        PokedexUiState(
            pokemonList = filtered,
            collectionMap = collection,
            filters = filters,
            layout = settings.layout,
            isLoading = status.first,
            error = status.second,
            totalCount = allPokemon.size,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PokedexUiState(isLoading = true),
    )

    init {
        sync()
    }

    fun sync(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            pokemonRepo.syncIndex(force = force)
                .onFailure {
                    _error.value = it.localizedMessage ?: "Failed to load Pokédex"
                }
            _isLoading.value = false
        }
    }

    fun setQuery(query: String) {
        _filters.update { it.copy(query = query) }
    }

    fun updateFilters(newFilters: PokedexFilters) {
        _filters.value = newFilters
    }

    fun toggleTypeFilter(type: String) {
        _filters.update { f ->
            val updated = if (f.types.contains(type)) f.types - type else f.types + type
            f.copy(types = updated)
        }
    }

    fun toggleGenFilter(gen: Int) {
        _filters.update { f ->
            val updated = if (f.generations.contains(gen)) f.generations - gen else f.generations + gen
            f.copy(generations = updated)
        }
    }

    fun resetFilters() {
        _filters.update { PokedexFilters(query = it.query) }
    }

    fun toggleLayout() {
        val current = uiState.value.layout
        val next = if (current == DexLayout.GRID) DexLayout.LIST else DexLayout.GRID
        viewModelScope.launch {
            settingsStore.setLayout(next)
        }
    }

    fun cycleStatus(speciesId: Int) {
        viewModelScope.launch {
            collectionRepo.cycleStatus(speciesId)
        }
    }

    fun toggleFavorite(speciesId: Int) {
        viewModelScope.launch {
            collectionRepo.toggleFavorite(speciesId)
        }
    }
}
