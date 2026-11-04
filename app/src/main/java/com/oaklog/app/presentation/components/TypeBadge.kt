package com.oaklog.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oaklog.app.presentation.theme.PokemonTypeColors

/** Type marker: a colored dot beside a neutral label, so type color stays an accent, not a fill. */
@Composable
fun TypeBadge(
    type: String,
    modifier: Modifier = Modifier,
    small: Boolean = false,
) {
    val shape = RoundedCornerShape(if (small) 6.dp else 8.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(
                horizontal = if (small) 6.dp else 10.dp,
                vertical = if (small) 3.dp else 5.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Spacer(
            Modifier
                .size(if (small) 6.dp else 8.dp)
                .clip(CircleShape)
                .background(PokemonTypeColors.color(type)),
        )
        Spacer(Modifier.width(if (small) 5.dp else 6.dp))
        Text(
            text = type.replaceFirstChar { it.uppercase() },
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = if (small) 10.sp else 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp,
        )
    }
}
