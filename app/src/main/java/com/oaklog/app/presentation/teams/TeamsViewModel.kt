package com.oaklog.app.presentation.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.domain.logic.TeamAnalysis
import com.oaklog.app.domain.logic.Attack
import com.oaklog.app.domain.logic.CoverageResult
import com.oaklog.app.domain.logic.Learnset
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.logic.TeamCoverage
import com.oaklog.app.domain.logic.SpeedTierEntry
import com.oaklog.app.domain.logic.TeamAnalyzer
import com.oaklog.app.domain.logic.TeamSuggestion
import com.oaklog.app.domain.logic.TeamSuggestions
import com.oaklog.app.domain.logic.TypeChart
import com.oaklog.app.domain.model.AbilityInfo
import com.oaklog.app.domain.model.AbilityRef
import com.oaklog.app.domain.model.LearnEntry
import com.oaklog.app.domain.model.MoveInfo
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

/** What a team member may legally hold: its real abilities and every move it can learn. */
data class MemberOptions(
    val abilities: List<AbilityRef>,
    val abilityInfo: Map<String, AbilityInfo>,
    val learnset: List<LearnEntry>,
)

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

    fun reorderMembers(teamId: Long, memberIds: List<Long>) {
        viewModelScope.launch {
            teamRepo.reorderMembers(teamId, memberIds)
        }
    }

    /** Base Speed per member, from cached detail payloads; members that fail to load are skipped. */
    suspend fun speedTiers(team: Team): List<SpeedTierEntry> {
        val speeds = team.members.mapNotNull { m ->
            pokemonRepo.getPokemonDetail(m.pokemonId.toString()).getOrNull()?.let { d ->
                (m.nickname?.takeIf { it.isNotBlank() } ?: d.displayName) to d.baseStat("speed")
            }
        }
        return TeamSuggestions.speedTiers(speeds)
    }

    fun suggestions(team: Team, analysis: TeamAnalysis, chart: TypeChart?): List<TeamSuggestion> {
        val tc = chart ?: _typeChart.value ?: return emptyList()
        return TeamSuggestions.generate(tc, analysis, team.members.size)
    }

    /** Null when the Pokémon's details can't be loaded (offline, nothing cached). */
    suspend fun memberOptions(member: TeamMember): MemberOptions? {
        val detail = pokemonRepo.getPokemonDetail(member.pokemonId.toString()).getOrNull() ?: return null
        // The numeric id sidesteps species/form name mismatches such as deoxys vs deoxys-normal.
        val learnset = pokemonRepo.getLearnset(detail.id.toString()).getOrDefault(emptyList())
        val info = detail.abilities.mapNotNull { ref ->
            pokemonRepo.getAbility(ref.name).getOrNull()?.let { ref.name to it }
        }.toMap()
        return MemberOptions(detail.abilities, info, learnset)
    }

    suspend fun moveInfo(name: String): MoveInfo? = pokemonRepo.getMove(Learnset.slug(name)).getOrNull()

    /**
     * Coverage from members' own types plus the damaging moves they have chosen. Move details come
     * from the repository cache, so this is cheap after a member's moves have been seen once.
     */
    suspend fun coverage(team: Team): CoverageResult? {
        val chart = _typeChart.value ?: return null
        val stab = team.members.flatMap { m ->
            val name = m.nickname ?: NameFormatter.pretty(m.pokemonName)
            m.types.map { Attack(name, null, it) }
        }
        val moves = team.members.flatMap { m ->
            val name = m.nickname ?: NameFormatter.pretty(m.pokemonName)
            m.moves.mapNotNull { move ->
                val info = moveInfo(move) ?: return@mapNotNull null
                val type = info.type ?: return@mapNotNull null
                // Status moves (and anything with no damage class) cover nothing.
                if (info.damageClass == null || info.damageClass == "status") null else Attack(name, move, type)
            }
        }
        return TeamCoverage.compute(chart, stab, moves)
    }

    fun computeAnalysis(team: Team, chart: TypeChart?): TeamAnalysis? {
        val tc = chart ?: _typeChart.value ?: return null
        val memberTypes = team.members.map { it.types }
        return TeamAnalyzer.analyze(tc, memberTypes)
    }
}

