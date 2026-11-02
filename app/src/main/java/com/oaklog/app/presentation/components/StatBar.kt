package com.oaklog.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatBar(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    maxStat: Int = 255,
) {
    var animated by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animated) (value.toFloat() / maxStat).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "StatProgress",
    )

    LaunchedEffect(Unit) {
        animated = true
    }

    val barColor = when {
        value < 50 -> Color(0xFFE53935)
        value < 80 -> Color(0xFFFB8C00)
        value < 110 -> Color(0xFF43A047)
        value < 140 -> Color(0xFF00ACC1)
        else -> Color(0xFF8E24AA)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(72.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )

        Text(
            text = value.toString(),
            modifier = Modifier.width(42.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}
