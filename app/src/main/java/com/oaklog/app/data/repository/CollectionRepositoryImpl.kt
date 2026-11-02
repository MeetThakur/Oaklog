package com.oaklog.app.data.repository

import com.oaklog.app.data.local.dao.CollectionDao
import com.oaklog.app.data.local.entity.CollectionEntity
import com.oaklog.app.domain.logic.CollectionStats
import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.CollectionStatus
import com.oaklog.app.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val dao: CollectionDao,
) : CollectionRepository {

    override fun observeCollection(): Flow<Map<Int, CollectionEntry>> =
        dao.observeAll().map { list ->
            list.associate { entity ->
                entity.speciesId to CollectionEntry(
                    speciesId = entity.speciesId,
                    status = runCatching { CollectionStatus.valueOf(entity.status) }.getOrDefault(CollectionStatus.NOT_CAUGHT),
                    shiny = entity.shiny,
                    favorite = entity.favorite,
                )
            }
        }

    override suspend fun getEntry(speciesId: Int): CollectionEntry? {
        val entity = dao.get(speciesId) ?: return null
        return CollectionEntry(
            speciesId = entity.speciesId,
            status = runCatching { CollectionStatus.valueOf(entity.status) }.getOrDefault(CollectionStatus.NOT_CAUGHT),
            shiny = entity.shiny,
            favorite = entity.favorite,
        )
    }

    override suspend fun setStatus(speciesId: Int, status: CollectionStatus) {
        val current = getEntry(speciesId) ?: CollectionEntry(speciesId = speciesId)
        val updated = current.copy(status = status, shiny = if (status == CollectionStatus.NOT_CAUGHT) false else current.shiny)
        save(updated)
    }

    override suspend fun cycleStatus(speciesId: Int) {
        val current = getEntry(speciesId) ?: CollectionEntry(speciesId = speciesId)
        val next = CollectionStats.cycleStatus(current)
        save(next)
    }

    override suspend fun toggleFavorite(speciesId: Int) {
        val current = getEntry(speciesId) ?: CollectionEntry(speciesId = speciesId)
        save(current.copy(favorite = !current.favorite))
    }

    override suspend fun toggleShiny(speciesId: Int) {
        val current = getEntry(speciesId) ?: CollectionEntry(speciesId = speciesId)
        val next = CollectionStats.toggleShiny(current)
        save(next)
    }

    private suspend fun save(entry: CollectionEntry) {
        dao.upsert(
            CollectionEntity(
                speciesId = entry.speciesId,
                status = entry.status.name,
                shiny = entry.shiny,
                favorite = entry.favorite,
            )
        )
    }
}
