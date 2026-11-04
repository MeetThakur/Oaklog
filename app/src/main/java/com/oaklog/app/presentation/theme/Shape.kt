package com.oaklog.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Large, friendly radii. Cards and tiles are 24 dp, controls step down from there.
val OakShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

val CardShape = RoundedCornerShape(24.dp)
val ChipShape = RoundedCornerShape(50)

/** Content sheet that slides up over a Pokémon's tinted header. */
val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
