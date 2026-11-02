package com.oaklog.app.data.repository

import com.oaklog.app.data.local.dao.PokemonIndexDao
import com.oaklog.app.data.local.dao.TeamDao
import com.oaklog.app.data.local.entity.TeamEntity
import com.oaklog.app.data.local.entity.TeamMemberEntity
import com.oaklog.app.domain.model.Team
import com.oaklog.app.domain.model.TeamMember
import com.oaklog.app.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepositoryImpl @Inject constructor(
    private val teamDao: TeamDao,
    private val indexDao: PokemonIndexDao,
) : TeamRepository {

    override fun observeTeams(): Flow<List<Team>> =
        combine(
            teamDao.observeTeams(),
            teamDao.observeAllMembers(),
            indexDao.observeAll()
        ) { teams, allMembers, indexList ->
            val indexMap = indexList.associateBy { it.id }
            val membersByTeam = allMembers.groupBy { it.teamId }

            teams.map { teamEntity ->
                val memberEntities = membersByTeam[teamEntity.id]?.sortedBy { it.position } ?: emptyList()
                val members = memberEntities.map { me ->
                    val species = indexMap[me.speciesId]
                    val types = listOfNotNull(species?.type1, species?.type2)
                    TeamMember(
                        id = me.id,
                        teamId = me.teamId,
                        position = me.position,
                        speciesId = me.speciesId,
                        pokemonId = me.pokemonId,
                        pokemonName = me.pokemonName,
                        nickname = me.nickname,
                        ability = me.ability,
                        moves = if (me.moves.isBlank()) emptyList() else me.moves.split(","),
                        types = types,
                        artworkUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${me.pokemonId}.png",
                    )
                }
                Team(
                    id = teamEntity.id,
                    name = teamEntity.name,
                    createdAt = teamEntity.createdAt,
                    updatedAt = teamEntity.updatedAt,
                    members = members,
                )
            }
        }

    override fun observeTeam(id: Long): Flow<Team?> =
        combine(
            teamDao.observeTeam(id),
            teamDao.observeMembers(id),
            indexDao.observeAll()
        ) { teamEntity, memberEntities, indexList ->
            if (teamEntity == null) return@combine null
            val indexMap = indexList.associateBy { it.id }
            val members = memberEntities.sortedBy { it.position }.map { me ->
                val species = indexMap[me.speciesId]
                val types = listOfNotNull(species?.type1, species?.type2)
                TeamMember(
                    id = me.id,
                    teamId = me.teamId,
                    position = me.position,
                    speciesId = me.speciesId,
                    pokemonId = me.pokemonId,
                    pokemonName = me.pokemonName,
                    nickname = me.nickname,
                    ability = me.ability,
                    moves = if (me.moves.isBlank()) emptyList() else me.moves.split(","),
                    types = types,
                    artworkUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${me.pokemonId}.png",
                )
            }
            Team(
                id = teamEntity.id,
                name = teamEntity.name,
                createdAt = teamEntity.createdAt,
                updatedAt = teamEntity.updatedAt,
                members = members,
            )
        }

    override suspend fun createTeam(name: String): Long {
        val now = System.currentTimeMillis()
        return teamDao.insertTeam(TeamEntity(name = name, createdAt = now, updatedAt = now))
    }

    override suspend fun renameTeam(id: Long, name: String) {
        val now = System.currentTimeMillis()
        teamDao.rename(id, name, now)
    }

    override suspend fun deleteTeam(id: Long) {
        teamDao.deleteTeam(id)
    }

    override suspend fun addMember(
        teamId: Long,
        speciesId: Int,
        pokemonId: Int,
        pokemonName: String,
    ): Long {
        val currentMembers = teamDao.members(teamId)
        if (currentMembers.size >= 6) {
            error("Team already has maximum of 6 Pokémon")
        }
        val nextPos = if (currentMembers.isEmpty()) 0 else (currentMembers.maxOf { it.position } + 1)
        val entity = TeamMemberEntity(
            teamId = teamId,
            position = nextPos,
            speciesId = speciesId,
            pokemonId = pokemonId,
            pokemonName = pokemonName,
            nickname = null,
            ability = null,
            moves = "",
        )
        val id = teamDao.insertMember(entity)
        teamDao.touch(teamId, System.currentTimeMillis())
        return id
    }

    override suspend fun updateMember(member: TeamMember) {
        val entity = TeamMemberEntity(
            id = member.id,
            teamId = member.teamId,
            position = member.position,
            speciesId = member.speciesId,
            pokemonId = member.pokemonId,
            pokemonName = member.pokemonName,
            nickname = member.nickname,
            ability = member.ability,
            moves = member.moves.take(4).joinToString(","),
        )
        teamDao.updateMember(entity)
        teamDao.touch(member.teamId, System.currentTimeMillis())
    }

    override suspend fun removeMember(memberId: Long) {
        teamDao.deleteMember(memberId)
    }

    override suspend fun reorderMembers(teamId: Long, memberIds: List<Long>) {
        val existing = teamDao.members(teamId).associateBy { it.id }
        val updated = memberIds.mapIndexedNotNull { index, id ->
            existing[id]?.copy(position = index)
        }
        if (updated.isNotEmpty()) {
            teamDao.updateMembers(updated)
            teamDao.touch(teamId, System.currentTimeMillis())
        }
    }
}
