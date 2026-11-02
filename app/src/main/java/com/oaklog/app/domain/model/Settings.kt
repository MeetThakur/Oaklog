package com.oaklog.app.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class DexLayout { GRID, LIST }

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    /** Version slug (e.g. "scarlet"), or null for "no preference". */
    val preferredVersion: String? = null,
    /** Regional pokédex slug, or null for the National Dex. */
    val defaultPokedex: String? = null,
    val layout: DexLayout = DexLayout.GRID,
)
