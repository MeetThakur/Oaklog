package com.oaklog.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.ChipShape

/*
 * Drop-in replacements for material3 Card / FilterChip. Screens import these explicitly, which wins
 * over the material3 wildcard import, so the whole app shares one flat, hairline-bordered surface
 * style without touching every call site.
 */

@Composable
fun Card(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    colors: CardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    @Suppress("UNUSED_PARAMETER") elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
    content: @Composable ColumnScope.() -> Unit,
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = border,
        content = content,
    )
}

@Composable
fun Card(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    colors: CardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    @Suppress("UNUSED_PARAMETER") elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
    content: @Composable ColumnScope.() -> Unit,
) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = border,
        content = content,
    )
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        shape = ChipShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = androidx.compose.ui.graphics.Color.Transparent,
            selectedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
    )
}
