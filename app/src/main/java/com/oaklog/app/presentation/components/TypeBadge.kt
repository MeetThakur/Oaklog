package com.oaklog.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.oaklog.app.presentation.theme.ChipShape
import com.oaklog.app.presentation.theme.TypeTint
import com.oaklog.app.presentation.theme.typeTint

/**
 * Type pill. Standing alone it paints itself in its own type tint, so it reads on any background.
 * On a card that is already tinted, pass [onTint] to get the translucent pill instead, otherwise the
 * pill would vanish into a card of the same color.
 */
@Composable
fun TypeBadge(
    type: String,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    onTint: TypeTint? = null,
) {
    val own = typeTint(type)
    val background = onTint?.chip ?: own.container
    val foreground = onTint?.onContainer ?: own.onContainer
    Text(
        text = type.replaceFirstChar { it.uppercase() },
        style = if (small) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
        color = foreground,
        modifier = modifier
            .clip(ChipShape)
            .background(background)
            .padding(horizontal = if (small) 9.dp else 12.dp, vertical = if (small) 3.dp else 5.dp),
    )
}
