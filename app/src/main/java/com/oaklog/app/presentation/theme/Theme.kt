package com.oaklog.app.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.oaklog.app.domain.model.ThemeMode

// "Graphite" (dark) and "Porcelain" (light): a restrained palette where the artwork is the brightest
// thing on screen. Pokémon type color comes in as an accent through TypeTint, never as a fill.
private val OakDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF2F3F5),
    onPrimary = Color(0xFF0E0F12),
    primaryContainer = Color(0xFFF2F3F5),
    onPrimaryContainer = Color(0xFF0E0F12),
    secondary = Color(0xFFB8BCC6),
    onSecondary = Color(0xFF0E0F12),
    secondaryContainer = Color(0xFF1C1F26),
    onSecondaryContainer = Color(0xFFF2F3F5),
    background = Color(0xFF0E0F12),
    onBackground = Color(0xFFF2F3F5),
    surface = Color(0xFF16181D),
    onSurface = Color(0xFFF2F3F5),
    surfaceVariant = Color(0xFF1C1F26),
    onSurfaceVariant = Color(0xFF8B909C),
    outline = Color(0xFF262A32),
    outlineVariant = Color(0xFF262A32),
    surfaceContainerLow = Color(0xFF16181D),
    surfaceContainer = Color(0xFF16181D),
    surfaceContainerHigh = Color(0xFF1C1F26),
    surfaceContainerHighest = Color(0xFF242830),
)

private val OakLightColorScheme = lightColorScheme(
    primary = Color(0xFF0F1115),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF0F1115),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF3B4048),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF0F1F4),
    onSecondaryContainer = Color(0xFF0F1115),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F1115),
    surface = Color(0xFFF6F7F9),
    onSurface = Color(0xFF0F1115),
    surfaceVariant = Color(0xFFF0F1F4),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFE6E8EC),
    outlineVariant = Color(0xFFE6E8EC),
    surfaceContainerLow = Color(0xFFF6F7F9),
    surfaceContainer = Color(0xFFF6F7F9),
    surfaceContainerHigh = Color(0xFFF0F1F4),
    surfaceContainerHighest = Color(0xFFE6E8EC),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OakLogTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> OakDarkColorScheme
        else -> OakLightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = OakTypography,
        shapes = OakShapes,
        content = content,
    )
}

