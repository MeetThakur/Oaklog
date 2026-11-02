package com.oaklog.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object PokemonTypeColors {
    val Normal = Color(0xFFA8A77A)
    val Fire = Color(0xFFEE8130)
    val Water = Color(0xFF6390F0)
    val Grass = Color(0xFF7AC74C)
    val Electric = Color(0xFFF7D02C)
    val Ice = Color(0xFF96D9D6)
    val Fighting = Color(0xFFC22E28)
    val Poison = Color(0xFFA33EA1)
    val Ground = Color(0xFFE2BF65)
    val Flying = Color(0xFFA98FF3)
    val Psychic = Color(0xFFF95587)
    val Bug = Color(0xFFA6B91A)
    val Rock = Color(0xFFB6A136)
    val Ghost = Color(0xFF735797)
    val Dragon = Color(0xFF6F35FC)
    val Steel = Color(0xFFB7B7CE)
    val Fairy = Color(0xFFD685AD)
    val Dark = Color(0xFF705746)
    val Stellar = Color(0xFF4291B8)
    val Unknown = Color(0xFF68A090)

    fun color(type: String): Color = when (type.lowercase().trim()) {
        "normal" -> Normal
        "fire" -> Fire
        "water" -> Water
        "grass" -> Grass
        "electric" -> Electric
        "ice" -> Ice
        "fighting" -> Fighting
        "poison" -> Poison
        "ground" -> Ground
        "flying" -> Flying
        "psychic" -> Psychic
        "bug" -> Bug
        "rock" -> Rock
        "ghost" -> Ghost
        "dragon" -> Dragon
        "steel" -> Steel
        "fairy" -> Fairy
        "dark" -> Dark
        "stellar" -> Stellar
        else -> Unknown
    }

    fun onColor(type: String): Color = when (type.lowercase().trim()) {
        "electric", "ice", "normal", "ground", "steel" -> Color(0xFF1E1E1E)
        else -> Color.White
    }

    fun gradient(types: List<String>): Brush {
        val colors = when {
            types.isEmpty() -> listOf(Unknown.copy(alpha = 0.5f), Unknown.copy(alpha = 0.2f))
            types.size == 1 -> listOf(color(types[0]).copy(alpha = 0.6f), color(types[0]).copy(alpha = 0.15f))
            else -> listOf(color(types[0]).copy(alpha = 0.6f), color(types[1]).copy(alpha = 0.25f))
        }
        return Brush.linearGradient(colors)
    }

    fun cardBackground(types: List<String>, isDark: Boolean): Color {
        val primaryType = types.firstOrNull() ?: "unknown"
        val baseColor = color(primaryType)
        return if (isDark) {
            baseColor.copy(alpha = 0.18f)
        } else {
            baseColor.copy(alpha = 0.12f)
        }
    }
}
