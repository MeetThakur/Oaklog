package com.oaklog.app.domain.repository

import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.CollectionStatus
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun observeCollection(): Flow<Map<Int, CollectionEntry>>
    suspend fun getEntry(speciesId: Int): CollectionEntry?
    suspend fun setStatus(speciesId: Int, status: CollectionStatus)
    suspend fun cycleStatus(speciesId: Int)
    suspend fun toggleFavorite(speciesId: Int)
    suspend fun toggleShiny(speciesId: Int)
}

