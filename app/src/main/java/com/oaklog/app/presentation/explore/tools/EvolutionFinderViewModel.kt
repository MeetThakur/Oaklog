package com.oaklog.app.presentation.explore.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.domain.model.EvoMethod
import com.oaklog.app.domain.model.EvolutionStep
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EvolutionFinderState(
    val steps: List<EvolutionStep> = emptyList(),
    val isLoading: Boolean = true,
    val progress: Pair<Int, Int>? = null,
    val failed: Boolean = false,
    val method: EvoMethod? = null,
    val item: String? = null,
    val query: String = "",
)

@HiltViewModel
class EvolutionFinderViewModel @Inject constructor(
    private val repo: PokemonRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EvolutionFinderState())
    val state: StateFlow<EvolutionFinderState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, failed = false) }
            repo.getEvolutionSteps { done, total -> _state.update { it.copy(progress = done to total) } }
                .onSuccess { steps -> _state.update { it.copy(steps = steps, isLoading = false, progress = null) } }
                .onFailure { _state.update { it.copy(isLoading = false, failed = true, progress = null) } }
        }
    }

    fun setQuery(value: String) = _state.update { it.copy(query = value) }

    /** Changing method drops an item that method can't have, so the list never goes mysteriously empty. */
    fun setMethod(method: EvoMethod?) = _state.update { it.copy(method = method, item = null) }

    fun setItem(item: String?) = _state.update { it.copy(item = item) }
}
