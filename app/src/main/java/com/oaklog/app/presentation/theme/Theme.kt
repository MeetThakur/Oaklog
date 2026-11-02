package com.oaklog.app.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.oaklog.app.domain.model.ThemeMode

private val OakDarkColorScheme = darkColorScheme(
    primary = OakRedLight,
    onPrimary = Color.Black,
    primaryContainer = OakRedContainerDark,
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = OakGold,
    onSecondary = Color.Black,
    background = OakDarkBackground,
    onBackground = OakTextPrimaryDark,
    surface = OakSurfaceDark,
    onSurface = OakTextPrimaryDark,
    surfaceVariant = OakSurfaceVariantDark,
    onSurfaceVariant = OakTextSecondaryDark,
    outline = OakOutlineDark,
)

private val OakLightColorScheme = lightColorScheme(
    primary = OakRedPrimary,
    onPrimary = Color.White,
    primaryContainer = OakRedContainerLight,
    onPrimaryContainer = OakRedDark,
    secondary = OakGold,
    onSecondary = Color.White,
    background = OakJournalBackground,
    onBackground = OakTextPrimaryLight,
    surface = OakSurfaceLight,
    onSurface = OakTextPrimaryLight,
    surfaceVariant = OakSurfaceVariantLight,
    onSurfaceVariant = OakTextSecondaryLight,
    outline = OakOutlineLight,
)

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OakTypography,
        shapes = OakShapes,
        content = content,
    )
}
