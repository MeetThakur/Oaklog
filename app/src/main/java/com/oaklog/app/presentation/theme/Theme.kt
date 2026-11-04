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
    primary = Color(0xFFFFB3B1),
    onPrimary = Color(0xFF68000F),
    primaryContainer = Color(0xFF93001D),
    onPrimaryContainer = Color(0xFFFFDAD8),
    secondary = Color(0xFFE5BDC0),
    onSecondary = Color(0xFF43292C),
    secondaryContainer = Color(0xFF5A3F43),
    onSecondaryContainer = Color(0xFFFFD9DC),
    tertiary = Color(0xFFF2BF48),
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF5C4300),
    onTertiaryContainer = Color(0xFFFFDF9A),
    background = OakDarkBackground,
    onBackground = OakTextPrimaryDark,
    surface = OakSurfaceDark,
    onSurface = OakTextPrimaryDark,
    surfaceVariant = OakSurfaceVariantDark,
    onSurfaceVariant = OakTextSecondaryDark,
    outline = OakOutlineDark,
    outlineVariant = OakOutlineDark,
    surfaceContainerLow = OakSurfaceDark,
    surfaceContainer = OakSurfaceDark,
    surfaceContainerHigh = OakSurfaceVariantDark,
    surfaceContainerHighest = Color(0xFF29292F),
)

private val OakLightColorScheme = lightColorScheme(
    primary = OakRedPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD8),
    onPrimaryContainer = Color(0xFF410006),
    secondary = Color(0xFF75565A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3DEE0),
    onSecondaryContainer = Color(0xFF2B1517),
    tertiary = Color(0xFF7A5900),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDF9A),
    onTertiaryContainer = Color(0xFF261A00),
    background = OakJournalBackground,
    onBackground = OakTextPrimaryLight,
    surface = OakSurfaceLight,
    onSurface = OakTextPrimaryLight,
    surfaceVariant = OakSurfaceVariantLight,
    onSurfaceVariant = OakTextSecondaryLight,
    outline = OakOutlineLight,
    outlineVariant = OakOutlineLight,
    surfaceContainerLow = OakSurfaceLight,
    surfaceContainer = OakSurfaceLight,
    surfaceContainerHigh = OakSurfaceVariantLight,
    surfaceContainerHighest = Color(0xFFE6E6EB),
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

