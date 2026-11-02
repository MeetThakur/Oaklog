package com.oaklog.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.data.prefs.SettingsDataStore
import com.oaklog.app.domain.model.DexLayout
import com.oaklog.app.domain.model.ThemeMode
import com.oaklog.app.domain.model.UserSettings
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val cachedCount: Int = 0,
    val cachedBytes: Long = 0,
    val isPreloading: Boolean = false,
    val preloadProgress: Pair<Int, Int> = 0 to 0, // done to total
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsStore: SettingsDataStore,
    private val pokemonRepo: PokemonRepository,
) : ViewModel() {

    private val _isPreloading = MutableStateFlow(false)
    private val _preloadProgress = MutableStateFlow(0 to 0)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsStore.settings,
        pokemonRepo.observeCacheSize(),
        _isPreloading,
        _preloadProgress,
    ) { settings, cacheSize, preloading, progress ->
        SettingsUiState(
            settings = settings,
            cachedCount = cacheSize.first,
            cachedBytes = cacheSize.second,
            isPreloading = preloading,
            preloadProgress = progress,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(),
    )

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            settingsStore.setTheme(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setDynamicColor(enabled)
        }
    }

    fun setPreferredVersion(version: String?) {
        viewModelScope.launch {
            settingsStore.setPreferredVersion(version)
        }
    }

    fun setLayout(layout: DexLayout) {
        viewModelScope.launch {
            settingsStore.setLayout(layout)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            pokemonRepo.clearCache()
        }
    }

    fun preloadGeneration(gen: Int) {
        viewModelScope.launch {
            _isPreloading.value = true
            pokemonRepo.preloadGeneration(gen) { done, total ->
                _preloadProgress.value = done to total
            }
            _isPreloading.value = false
        }
    }
}
