package com.oaklog.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * The surface colors a Pokémon's type paints onto its card and detail header. Derived from the one
 * hue in [PokemonTypeColors] by mixing, so there is a single source of truth and every pair keeps the
 * same contrast: container/onContainer is at least 7:1 for every type in both modes (see TypeTintTest).
 */
@Immutable
data class TypeTint(
    /** Card and header fill. */
    val container: Color,
    /** Text and icons on [container]. */
    val onContainer: Color,
    /** The disc the artwork sits on, a step stronger than [container]. */
    val art: Color,
    /** Translucent pill behind type names and stats on [container]. */
    val chip: Color,
)

/** Pure so it can be unit tested; [typeTint] is the composable entry point. */
fun typeTintFor(type: String?, dark: Boolean): TypeTint {
    val base = PokemonTypeColors.color(type ?: "unknown")
    return if (dark) {
        // A deep wash of the hue over the dark panel, with light text mixed from the hue.
        TypeTint(
            container = lerp(DarkSurface, base, 0.34f),
            onContainer = lerp(base, Color.White, 0.92f),
            art = lerp(DarkSurface, base, 0.60f),
            chip = Color.White.copy(alpha = 0.12f),
        )
    } else {
        TypeTint(
            container = lerp(base, Color.White, 0.62f),
            onContainer = lerp(base, Color.Black, 0.74f),
            art = lerp(base, Color.White, 0.28f),
            chip = Color.White.copy(alpha = 0.55f),
        )
    }
}

@Composable
fun typeTint(type: String?): TypeTint {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return typeTintFor(type, dark)
}

/** Neutral panel used behind content that isn't a Pokémon (analysis, settings, databases). */
fun neutralPanel(dark: Boolean): Color = if (dark) DarkSurface else LightPanel
