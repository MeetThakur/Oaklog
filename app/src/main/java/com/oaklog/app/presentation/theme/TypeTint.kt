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
        // A faint wash of the hue over the graphite surface, with near-white text carrying a hint of it.
        TypeTint(
            container = lerp(GraphiteSurface, base, 0.17f),
            onContainer = lerp(Color(0xFFF2F3F5), base, 0.08f),
            art = lerp(GraphiteSurface, base, 0.34f),
            chip = Color.White.copy(alpha = 0.09f),
        )
    } else {
        TypeTint(
            container = lerp(Color.White, base, 0.15f),
            onContainer = lerp(Color(0xFF0F1115), base, 0.12f),
            art = lerp(Color.White, base, 0.30f),
            chip = Color.Black.copy(alpha = 0.06f),
        )
    }
}

@Composable
fun typeTint(type: String?): TypeTint {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return typeTintFor(type, dark)
}

/** Neutral panel used behind content that isn't a Pokémon (analysis, settings, databases). */
fun neutralPanel(dark: Boolean): Color = if (dark) GraphiteSurface else Color(0xFFF6F7F9)

/** Surface the dark tints are mixed over; matches the dark scheme's `surface`. */
private val GraphiteSurface = Color(0xFF16181D)

/**
 * Meaning-carrying colors for pills and icons: [Bad] for weaknesses, [Good] for resistances and
 * completeness, [Special] for immunities. Built from type tints so they sit correctly on both palettes.
 */
enum class Tone(internal val hue: String) { Bad("fighting"), Good("grass"), Special("ghost"), Warn("fire") }

@Composable
fun toneTint(tone: Tone): TypeTint = typeTint(tone.hue)

/** A solid mid-tone for bars and icons drawn straight on a card, readable on both palettes. */
fun toneAccent(tone: Tone): Color = PokemonTypeColors.color(tone.hue)
