package com.oaklog.app.presentation.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.domain.logic.CollectionStats
import com.oaklog.app.domain.logic.CollectionSummary
import com.oaklog.app.domain.logic.Progress
import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.CollectionStatus
import com.oaklog.app.domain.model.PokemonSummary
import com.oaklog.app.domain.repository.CollectionRepository
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CollectionFilterTab { ALL, CAUGHT, SEEN, UNCAUGHT, SHINIES, FAVORITES }

data class CollectionUiState(
    val summary: CollectionSummary = CollectionSummary(
        caught = Progress(0, 0),
        seen = 0,
        shiny = 0,
        favorites = 0,
        byGeneration = emptyMap(),
    ),
    val displayList: List<PokemonSummary> = emptyList(),
    val collectionMap: Map<Int, CollectionEntry> = emptyMap(),
    val activeTab: CollectionFilterTab = CollectionFilterTab.ALL,
    val selectedGen: Int? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val pokemonRepo: PokemonRepository,
    private val collectionRepo: CollectionRepository,
) : ViewModel() {

    private val _activeTab = MutableStateFlow(CollectionFilterTab.ALL)
    private val _selectedGen = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<CollectionUiState> = combine(
        pokemonRepo.observeIndex(),
        collectionRepo.observeCollection(),
        _activeTab,
        _selectedGen,
    ) { allPokemon, collection, tab, gen ->
        val summary = CollectionStats.summarize(allPokemon, collection)
        val filtered = allPokemon.filter { p ->
            val entry = collection[p.id]
            val matchesGen = gen == null || p.generation == gen
            val matchesTab = when (tab) {
                CollectionFilterTab.ALL -> true
                CollectionFilterTab.CAUGHT -> entry?.status == CollectionStatus.CAUGHT
                CollectionFilterTab.SEEN -> entry?.status == CollectionStatus.SEEN
                CollectionFilterTab.UNCAUGHT -> entry == null || entry.status == CollectionStatus.NOT_CAUGHT
                CollectionFilterTab.SHINIES -> entry?.shiny == true
                CollectionFilterTab.FAVORITES -> entry?.favorite == true
            }
            matchesGen && matchesTab
        }
        CollectionUiState(
            summary = summary,
            displayList = filtered,
            collectionMap = collection,
            activeTab = tab,
            selectedGen = gen,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CollectionUiState(isLoading = true),
    )

    fun selectTab(tab: CollectionFilterTab) {
        _activeTab.value = tab
    }

    fun selectGen(gen: Int?) {
        _selectedGen.value = if (_selectedGen.value == gen) null else gen
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

    fun toggleShiny(speciesId: Int) {
        viewModelScope.launch {
            collectionRepo.toggleShiny(speciesId)
        }
    }
}

