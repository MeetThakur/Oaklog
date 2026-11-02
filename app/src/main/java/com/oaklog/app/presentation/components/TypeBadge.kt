package com.oaklog.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oaklog.app.presentation.theme.PokemonTypeColors

@Composable
fun TypeBadge(
    type: String,
    modifier: Modifier = Modifier,
    small: Boolean = false,
) {
    val bgColor = PokemonTypeColors.color(type)
    val textColor = PokemonTypeColors.onColor(type)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (small) 6.dp else 10.dp))
            .background(bgColor)
            .padding(
                horizontal = if (small) 6.dp else 10.dp,
                vertical = if (small) 2.dp else 4.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.uppercase(),
            color = textColor,
            fontSize = if (small) 10.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
    }
}
