package com.oaklog.app.data.local

import android.content.Context
import com.oaklog.app.data.local.dao.CachedResourceDao
import com.oaklog.app.data.local.entity.CachedResourceEntity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first store for PokéAPI payloads.
 *
 * - Fresh cache hit: returned without touching the network.
 * - Stale/missing: fetched, persisted, returned. If the fetch fails and a stale copy exists, the
 *   stale copy wins, so the app keeps working offline.
 * - Concurrent requests for the same key share one network call (fair-use friendly).
 */
@Singleton
class ResourceCache @Inject constructor(
    private val dao: CachedResourceDao,
    private val json: Json,
    @ApplicationContext private val context: Context,
) {
    private var schemaChecked = false
    private val inFlight = HashMap<String, CompletableDeferred<String>>()
    private val lock = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Payloads are stored re-encoded through our DTOs, so a field added to a DTO later is missing
     * from every entry saved before. When [SCHEMA_VERSION] moves, drop the old entries once and let
     * them refetch with the new fields. Only cached API responses go: not the index, collection or teams.
     */
    private suspend fun ensureSchema() {
        if (schemaChecked) return
        lock.withLock {
            if (schemaChecked) return
            val prefs = context.getSharedPreferences("oaklog_cache_meta", Context.MODE_PRIVATE)
            if (prefs.getInt("schema", 0) < SCHEMA_VERSION) {
                dao.clear()
                prefs.edit().putInt("schema", SCHEMA_VERSION).apply()
            }
            schemaChecked = true
        }
    }

    suspend fun <T> get(
        key: String,
        serializer: KSerializer<T>,
        maxAgeMs: Long = DETAIL_TTL_MS,
        fetch: suspend () -> T,
    ): T {
        ensureSchema()
        val cached = dao.get(key)
        val now = System.currentTimeMillis()
        if (cached != null && now - cached.fetchedAt < maxAgeMs) {
            decode(serializer, cached.json)?.let { return it }
        }
        val text = try {
            shared(key) { json.encodeToString(serializer, fetch()) }
        } catch (t: Throwable) {
            if (t is kotlinx.coroutines.CancellationException) throw t
            val stale = cached?.let { decode(serializer, it.json) }
            return stale ?: throw t
        }
        return json.decodeFromString(serializer, text)
    }

    /** Returns the cached value regardless of age, or null. Never touches the network. */
    suspend fun <T> peek(key: String, serializer: KSerializer<T>): T? {
        ensureSchema()
        return dao.get(key)?.let { decode(serializer, it.json) }
    }

    suspend fun isCached(key: String): Boolean {
        ensureSchema()
        return dao.get(key) != null
    }

    private fun <T> decode(serializer: KSerializer<T>, text: String): T? =
        runCatching { json.decodeFromString(serializer, text) }.getOrNull()

    private suspend fun shared(key: String, produce: suspend () -> String): String {
        var mine: CompletableDeferred<String>? = null
        val existing = lock.withLock {
            inFlight[key] ?: CompletableDeferred<String>().also { mine = it; inFlight[key] = it }
        }
        if (mine == null) return existing.await()
        val deferred = mine!!
        try {
            // Run in our own scope so a cancelled first caller doesn't fail the other waiters.
            val text = scope.async { produce() }.await()
            dao.put(CachedResourceEntity(key, text, System.currentTimeMillis()))
            deferred.complete(text)
            return text
        } catch (t: Throwable) {
            deferred.completeExceptionally(t)
            throw t
        } finally {
            lock.withLock { inFlight.remove(key) }
        }
    }

    companion object {
        /**
         * Bump whenever a DTO gains a field the app reads. Version 2: location lists on regions,
         * cries and female sprites on Pokémon.
         */
        const val SCHEMA_VERSION = 2
        const val DAY_MS = 24L * 60 * 60 * 1000
        const val DETAIL_TTL_MS = 30 * DAY_MS
        const val LIST_TTL_MS = 7 * DAY_MS
    }
}
