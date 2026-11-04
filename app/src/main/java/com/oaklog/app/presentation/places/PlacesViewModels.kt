package com.oaklog.app.presentation.places

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.domain.model.AreaSpawn
import com.oaklog.app.domain.model.NamedEntry
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegionPlacesState(
    val locations: List<NamedEntry> = emptyList(),
    val isLoading: Boolean = true,
    val failed: Boolean = false,
)

@HiltViewModel
class RegionPlacesViewModel @Inject constructor(
    private val repo: PokemonRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val region: String = checkNotNull(savedStateHandle["name"])

    private val _state = MutableStateFlow(RegionPlacesState())
    val state: StateFlow<RegionPlacesState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = RegionPlacesState(isLoading = true)
            repo.getRegionLocations(region)
                .onSuccess { _state.value = RegionPlacesState(it.sortedBy { l -> l.name }, isLoading = false) }
                .onFailure { _state.value = RegionPlacesState(isLoading = false, failed = true) }
        }
    }
}

/** An area and, once opened, what lives there. Null spawns means not loaded yet. */
data class AreaState(
    val area: NamedEntry,
    val spawns: List<AreaSpawn>? = null,
    val failed: Boolean = false,
)

data class LocationState(
    val areas: List<AreaState> = emptyList(),
    val isLoading: Boolean = true,
    val failed: Boolean = false,
)

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val repo: PokemonRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val location: String = checkNotNull(savedStateHandle["name"])

    private val _state = MutableStateFlow(LocationState())
    val state: StateFlow<LocationState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LocationState(isLoading = true)
            repo.getLocationAreas(location)
                .onSuccess { areas ->
                    _state.value = LocationState(areas.map { AreaState(it) }, isLoading = false)
                    // Most locations have one area, so open it straight away.
                    areas.singleOrNull()?.let { loadArea(it.name) }
                }
                .onFailure { _state.value = LocationState(isLoading = false, failed = true) }
        }
    }

    fun loadArea(name: String) {
        if (_state.value.areas.firstOrNull { it.area.name == name }?.spawns != null) return
        viewModelScope.launch(Dispatchers.IO) {
            val result = repo.getAreaSpawns(name)
            _state.update { s ->
                s.copy(areas = s.areas.map {
                    if (it.area.name != name) it
                    else it.copy(spawns = result.getOrNull(), failed = result.isFailure)
                })
            }
        }
    }
}
