package com.oaklog.app.presentation.explore.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.domain.model.BerryInfo
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BerriesState(
    val berries: List<BerryInfo> = emptyList(),
    val isLoading: Boolean = true,
    val progress: Pair<Int, Int>? = null,
    val failed: Boolean = false,
    val flavor: String? = null,
    val query: String = "",
)

@HiltViewModel
class BerriesViewModel @Inject constructor(
    private val repo: PokemonRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BerriesState())
    val state: StateFlow<BerriesState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, failed = false) }
            repo.getBerries { done, total -> _state.update { it.copy(progress = done to total) } }
                .onSuccess { list -> _state.update { it.copy(berries = list, isLoading = false, progress = null) } }
                .onFailure { _state.update { it.copy(isLoading = false, failed = true, progress = null) } }
        }
    }

    fun setQuery(value: String) = _state.update { it.copy(query = value) }
    fun setFlavor(flavor: String?) = _state.update { it.copy(flavor = flavor) }
}
