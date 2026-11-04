package com.oaklog.app.presentation.explore.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.domain.logic.TypeChart
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

enum class CalcRole { STATS, ATTACKER, DEFENDER }

data class CalculatorUiState(
    val stats: PokemonDetail? = null,
    val attacker: PokemonDetail? = null,
    val defender: PokemonDetail? = null,
    val typeChart: TypeChart? = null,
    val index: List<PokemonSummary> = emptyList(),
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val repo: PokemonRepository,
) : ViewModel() {

    private val local = MutableStateFlow(CalculatorUiState())

    val uiState: StateFlow<CalculatorUiState> = combine(local, repo.observeIndex()) { s, index ->
        s.copy(index = index)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalculatorUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getTypeChart().onSuccess { chart -> local.value = local.value.copy(typeChart = chart) }
        }
    }

    fun pick(role: CalcRole, speciesId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getPokemonDetail(speciesId.toString()).onSuccess { d ->
                local.value = when (role) {
                    CalcRole.STATS -> local.value.copy(stats = d)
                    CalcRole.ATTACKER -> local.value.copy(attacker = d)
                    CalcRole.DEFENDER -> local.value.copy(defender = d)
                }
            }
        }
    }
}
