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

private val OakDarkColorScheme = darkColorScheme(
    primary = DarkInk,
    onPrimary = DarkBackground,
    primaryContainer = DarkInk,
    onPrimaryContainer = DarkBackground,
    secondary = Color(0xFFCFCFD6),
    onSecondary = DarkBackground,
    secondaryContainer = DarkPanelHigh,
    onSecondaryContainer = DarkInk,
    tertiary = OakShinyGold,
    onTertiary = Color(0xFF1A1300),
    tertiaryContainer = Color(0xFF3A2D00),
    onTertiaryContainer = Color(0xFFFFE08A),
    background = DarkBackground,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkPanelHigh,
    onSurfaceVariant = DarkInkSecondary,
    outline = DarkRule,
    outlineVariant = DarkRule,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkPanelHigh,
    surfaceContainerHighest = DarkPanelHighest,
)

private val OakLightColorScheme = lightColorScheme(
    primary = LightInk,
    onPrimary = Color.White,
    primaryContainer = LightInk,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF3D3D3A),
    onSecondary = Color.White,
    secondaryContainer = LightPanelHigh,
    onSecondaryContainer = LightInk,
    tertiary = Color(0xFF7A5900),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE9A8),
    onTertiaryContainer = Color(0xFF261A00),
    background = LightBackground,
    onBackground = LightInk,
    surface = LightPanel,
    onSurface = LightInk,
    surfaceVariant = LightPanelHigh,
    onSurfaceVariant = LightInkSecondary,
    outline = LightRule,
    outlineVariant = LightRule,
    surfaceContainerLow = LightPanel,
    surfaceContainer = LightPanel,
    surfaceContainerHigh = LightPanelHigh,
    surfaceContainerHighest = LightPanelHighest,
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

