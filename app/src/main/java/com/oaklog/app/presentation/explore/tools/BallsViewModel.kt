package com.oaklog.app.presentation.explore.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.domain.model.ItemInfo
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BallsState(
    val balls: List<ItemInfo> = emptyList(),
    val isLoading: Boolean = true,
    val failed: Boolean = false,
    val category: String? = null,
    val query: String = "",
)

@HiltViewModel
class BallsViewModel @Inject constructor(
    private val repo: PokemonRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(BallsState())
    val state: StateFlow<BallsState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, failed = false) }
            repo.getBalls()
                .onSuccess { list -> _state.update { it.copy(balls = list, isLoading = false) } }
                .onFailure { _state.update { it.copy(isLoading = false, failed = true) } }
        }
    }

    fun setQuery(value: String) = _state.update { it.copy(query = value) }
    fun setCategory(value: String?) = _state.update { it.copy(category = value) }
}
