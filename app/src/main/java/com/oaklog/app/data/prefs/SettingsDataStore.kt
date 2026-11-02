package com.oaklog.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.oaklog.app.domain.model.DexLayout
import com.oaklog.app.domain.model.ThemeMode
import com.oaklog.app.domain.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "oaklog_settings")

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val dynamic = booleanPreferencesKey("dynamic_color")
        val version = stringPreferencesKey("preferred_version")
        val pokedex = stringPreferencesKey("default_pokedex")
        val layout = stringPreferencesKey("layout")
        val lastIndexSync = longPreferencesKey("last_index_sync")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            themeMode = p[Keys.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            dynamicColor = p[Keys.dynamic] ?: false,
            preferredVersion = p[Keys.version],
            defaultPokedex = p[Keys.pokedex],
            layout = p[Keys.layout]?.let { runCatching { DexLayout.valueOf(it) }.getOrNull() } ?: DexLayout.GRID,
        )
    }

    val lastIndexSync: Flow<Long> = context.dataStore.data.map { it[Keys.lastIndexSync] ?: 0L }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[Keys.theme] = mode.name }
    suspend fun setDynamicColor(enabled: Boolean) = context.dataStore.edit { it[Keys.dynamic] = enabled }
    suspend fun setPreferredVersion(version: String?) = context.dataStore.edit {
        if (version == null) it.remove(Keys.version) else it[Keys.version] = version
    }
    suspend fun setDefaultPokedex(slug: String?) = context.dataStore.edit {
        if (slug == null) it.remove(Keys.pokedex) else it[Keys.pokedex] = slug
    }
    suspend fun setLayout(layout: DexLayout) = context.dataStore.edit { it[Keys.layout] = layout.name }
    suspend fun setLastIndexSync(time: Long) = context.dataStore.edit { it[Keys.lastIndexSync] = time }
}
