package com.oaklog.app.domain.repository

import com.oaklog.app.domain.model.Team
import com.oaklog.app.domain.model.TeamMember
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun observeTeams(): Flow<List<Team>>
    fun observeTeam(id: Long): Flow<Team?>
    suspend fun createTeam(name: String): Long
    suspend fun renameTeam(id: Long, name: String)
    suspend fun deleteTeam(id: Long)
    suspend fun addMember(teamId: Long, speciesId: Int, pokemonId: Int, pokemonName: String): Long
    suspend fun updateMember(member: TeamMember)
    suspend fun removeMember(memberId: Long)
    suspend fun reorderMembers(teamId: Long, memberIds: List<Long>)
}
