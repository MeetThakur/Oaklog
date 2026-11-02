package com.oaklog.app.data.local.dao

import androidx.room.*
import com.oaklog.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedResourceDao {
    @Query("SELECT * FROM cached_resource WHERE `key` = :key")
    suspend fun get(key: String): CachedResourceEntity?

    @Upsert
    suspend fun put(entity: CachedResourceEntity)

    @Query("SELECT COUNT(*) FROM cached_resource")
    fun count(): Flow<Int>

    @Query("SELECT COALESCE(SUM(LENGTH(json)), 0) FROM cached_resource")
    fun totalBytes(): Flow<Long>

    @Query("SELECT `key` FROM cached_resource WHERE `key` LIKE :prefix || '%'")
    suspend fun keysWithPrefix(prefix: String): List<String>

    @Query("DELETE FROM cached_resource")
    suspend fun clear()
}

@Dao
interface PokemonIndexDao {
    @Query("SELECT * FROM pokemon_index ORDER BY id")
    fun observeAll(): Flow<List<PokemonIndexEntity>>

    @Query("SELECT * FROM pokemon_index WHERE id = :id")
    suspend fun get(id: Int): PokemonIndexEntity?

    @Query("SELECT COUNT(*) FROM pokemon_index")
    suspend fun count(): Int

    @Query("SELECT id FROM pokemon_index WHERE baseStatTotal IS NOT NULL AND isLegendary IS NOT NULL")
    suspend fun hydratedIds(): List<Int>

    @Upsert
    suspend fun upsertAll(items: List<PokemonIndexEntity>)

    @Upsert
    suspend fun upsert(item: PokemonIndexEntity)

    @Query("DELETE FROM pokemon_index")
    suspend fun clear()
}

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collection")
    fun observeAll(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collection WHERE speciesId = :id")
    suspend fun get(id: Int): CollectionEntity?

    @Upsert
    suspend fun upsert(entity: CollectionEntity)

    @Upsert
    suspend fun upsertAll(entities: List<CollectionEntity>)
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM team ORDER BY updatedAt DESC")
    fun observeTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM team_member ORDER BY teamId, position")
    fun observeAllMembers(): Flow<List<TeamMemberEntity>>

    @Query("SELECT * FROM team WHERE id = :id")
    fun observeTeam(id: Long): Flow<TeamEntity?>

    @Query("SELECT * FROM team_member WHERE teamId = :teamId ORDER BY position")
    fun observeMembers(teamId: Long): Flow<List<TeamMemberEntity>>

    @Query("SELECT * FROM team_member WHERE teamId = :teamId ORDER BY position")
    suspend fun members(teamId: Long): List<TeamMemberEntity>

    @Insert
    suspend fun insertTeam(team: TeamEntity): Long

    @Query("UPDATE team SET name = :name, updatedAt = :now WHERE id = :id")
    suspend fun rename(id: Long, name: String, now: Long)

    @Query("UPDATE team SET updatedAt = :now WHERE id = :id")
    suspend fun touch(id: Long, now: Long)

    @Query("DELETE FROM team WHERE id = :id")
    suspend fun deleteTeam(id: Long)

    @Insert
    suspend fun insertMember(member: TeamMemberEntity): Long

    @Update
    suspend fun updateMember(member: TeamMemberEntity)

    @Update
    suspend fun updateMembers(members: List<TeamMemberEntity>)

    @Query("DELETE FROM team_member WHERE id = :id")
    suspend fun deleteMember(id: Long)
}
