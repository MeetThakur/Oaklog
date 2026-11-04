package com.oaklog.app.presentation.compare

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.domain.model.PokemonDetail
import com.oaklog.app.domain.model.PokemonSummary
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Side { A, B }

data class CompareUiState(
    val a: PokemonDetail? = null,
    val b: PokemonDetail? = null,
    val loadingA: Boolean = false,
    val loadingB: Boolean = false,
    val failed: Boolean = false,
    val index: List<PokemonSummary> = emptyList(),
)

@HiltViewModel
class CompareViewModel @Inject constructor(
    private val repo: PokemonRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val local = MutableStateFlow(CompareUiState())

    val uiState: StateFlow<CompareUiState> = combine(local, repo.observeIndex()) { s, index ->
        s.copy(index = index)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompareUiState())

    init {
        savedStateHandle.get<String>("a")?.toIntOrNull()?.let { select(Side.A, it) }
        savedStateHandle.get<String>("b")?.toIntOrNull()?.let { select(Side.B, it) }
    }

    fun select(side: Side, speciesId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            local.value = when (side) {
                Side.A -> local.value.copy(loadingA = true, failed = false)
                Side.B -> local.value.copy(loadingB = true, failed = false)
            }
            val result = repo.getPokemonDetail(speciesId.toString())
            local.value = when (side) {
                Side.A -> local.value.copy(a = result.getOrNull() ?: local.value.a, loadingA = false, failed = result.isFailure)
                Side.B -> local.value.copy(b = result.getOrNull() ?: local.value.b, loadingB = false, failed = result.isFailure)
            }
        }
    }
}
