package com.oaklog.app.presentation.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.domain.logic.TeamAnalysis
import com.oaklog.app.domain.logic.TeamAnalyzer
import com.oaklog.app.domain.logic.TypeChart
import com.oaklog.app.domain.model.PokemonSummary
import com.oaklog.app.domain.model.Team
import com.oaklog.app.domain.model.TeamMember
import com.oaklog.app.domain.repository.PokemonRepository
import com.oaklog.app.domain.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamsUiState(
    val teams: List<Team> = emptyList(),
    val allPokemon: List<PokemonSummary> = emptyList(),
    val typeChart: TypeChart? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class TeamsViewModel @Inject constructor(
    private val teamRepo: TeamRepository,
    private val pokemonRepo: PokemonRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _typeChart = MutableStateFlow<TypeChart?>(null)

    val uiState: StateFlow<TeamsUiState> = combine(
        teamRepo.observeTeams(),
        pokemonRepo.observeIndex(),
        _typeChart,
        _isLoading,
    ) { teams, index, chart, loading ->
        TeamsUiState(
            teams = teams,
            allPokemon = index,
            typeChart = chart,
            isLoading = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TeamsUiState(isLoading = true),
    )

    init {
        loadTypeChart()
    }

    private fun loadTypeChart() {
        viewModelScope.launch(Dispatchers.IO) {
            pokemonRepo.getTypeChart().onSuccess { chart ->
                _typeChart.value = chart
            }
        }
    }

    fun createTeam(name: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = teamRepo.createTeam(name.trim().ifEmpty { "New Team" })
            onCreated(id)
        }
    }

    fun renameTeam(id: Long, name: String) {
        viewModelScope.launch {
            teamRepo.renameTeam(id, name.trim().ifEmpty { "Unnamed Team" })
        }
    }

    fun deleteTeam(id: Long) {
        viewModelScope.launch {
            teamRepo.deleteTeam(id)
        }
    }

    fun addMember(teamId: Long, pokemon: PokemonSummary) {
        viewModelScope.launch {
            runCatching {
                teamRepo.addMember(
                    teamId = teamId,
                    speciesId = pokemon.id,
                    pokemonId = pokemon.id,
                    pokemonName = pokemon.name,
                )
            }
        }
    }

    fun updateMember(member: TeamMember) {
        viewModelScope.launch {
            teamRepo.updateMember(member)
        }
    }

    fun removeMember(memberId: Long) {
        viewModelScope.launch {
            teamRepo.removeMember(memberId)
        }
    }

    fun computeAnalysis(team: Team, chart: TypeChart?): TeamAnalysis? {
        val tc = chart ?: _typeChart.value ?: return null
        val memberTypes = team.members.map { it.types }
        return TeamAnalyzer.analyze(tc, memberTypes)
    }
}

