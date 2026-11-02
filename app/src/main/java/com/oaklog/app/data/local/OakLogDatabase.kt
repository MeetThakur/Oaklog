package com.oaklog.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.oaklog.app.data.local.dao.*
import com.oaklog.app.data.local.entity.*

@Database(
    entities = [
        CachedResourceEntity::class,
        PokemonIndexEntity::class,
        CollectionEntity::class,
        TeamEntity::class,
        TeamMemberEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class OakLogDatabase : RoomDatabase() {
    abstract fun cachedResourceDao(): CachedResourceDao
    abstract fun pokemonIndexDao(): PokemonIndexDao
    abstract fun collectionDao(): CollectionDao
    abstract fun teamDao(): TeamDao

    companion object {
        const val NAME = "oaklog_cache.db"
    }
}
