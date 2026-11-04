@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.oaklog.app.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.oaklog.app.R

// One bold geometric sans does the whole job. It is a variable font, so one file serves every weight.
private fun jakarta(weight: Int) = Font(
    resId = R.font.plus_jakarta_sans,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val OakSans = FontFamily(jakarta(400), jakarta(500), jakarta(600), jakarta(700), jakarta(800))

private fun style(
    size: Int,
    line: Int,
    weight: FontWeight,
    tracking: Double = 0.0,
    tabular: Boolean = false,
) = TextStyle(
    fontFamily = OakSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
    fontFeatureSettings = if (tabular) "tnum" else null,
)

// Heavy at display sizes, calm in body text. Numbers use tabular figures so columns line up.
val OakTypography = Typography(
    displaySmall = style(40, 46, FontWeight.ExtraBold, -1.0),
    headlineLarge = style(32, 38, FontWeight.ExtraBold, -0.8),
    headlineMedium = style(28, 34, FontWeight.ExtraBold, -0.6),
    headlineSmall = style(24, 30, FontWeight.Bold, -0.4),
    titleLarge = style(22, 28, FontWeight.Bold, -0.3),
    titleMedium = style(16, 22, FontWeight.Bold, -0.1),
    titleSmall = style(14, 20, FontWeight.Bold, 0.0),
    bodyLarge = style(16, 24, FontWeight.Normal),
    bodyMedium = style(14, 21, FontWeight.Normal),
    bodySmall = style(12, 17, FontWeight.Normal),
    labelLarge = style(14, 20, FontWeight.SemiBold, 0.1),
    labelMedium = style(12, 16, FontWeight.SemiBold, 0.2, tabular = true),
    labelSmall = style(11, 16, FontWeight.Medium, 0.3, tabular = true),
)
