package com.oaklog.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.CollectionStatus
import com.oaklog.app.presentation.theme.OakCaughtGreen
import com.oaklog.app.presentation.theme.OakFavoriteRed
import com.oaklog.app.presentation.theme.OakSeenBlue
import com.oaklog.app.presentation.theme.OakShinyGold

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    message: String = "Loading Pokédex data...",
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LoadingIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

/**
 * Empty state with a tonal icon, a short explanation and an optional single action.
 * Callers pass an icon that names the thing that's missing, not a generic placeholder.
 */
@Composable
fun EmptyView(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
fun CollectionStatusButton(
    entry: CollectionEntry?,
    onCycleStatus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = entry?.status ?: CollectionStatus.NOT_CAUGHT
    val (icon, color, text) = when (status) {
        CollectionStatus.CAUGHT -> Triple(Icons.Filled.CheckCircle, OakCaughtGreen, "Caught")
        CollectionStatus.SEEN -> Triple(Icons.Filled.Visibility, OakSeenBlue, "Seen")
        CollectionStatus.NOT_CAUGHT -> Triple(Icons.Outlined.CheckCircleOutline, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), "Uncaught")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (status == CollectionStatus.NOT_CAUGHT) MaterialTheme.colorScheme.surface else color.copy(alpha = 0.12f))
            .border(1.dp, if (status == CollectionStatus.NOT_CAUGHT) MaterialTheme.colorScheme.outline else color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { onCycleStatus() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (status == CollectionStatus.NOT_CAUGHT) MaterialTheme.colorScheme.onSurfaceVariant else color,
            )
        }
    }
}

@Composable
fun FavoriteIconButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier.size(32.dp),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) OakFavoriteRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp),
        )
    }
}


/** Icon-only caught/seen toggle for dense lists; tap cycles status. */
@Composable
fun StatusDot(
    entry: CollectionEntry?,
    onCycleStatus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = entry?.status ?: CollectionStatus.NOT_CAUGHT
    val (icon, color, label) = when (status) {
        CollectionStatus.CAUGHT -> Triple(Icons.Filled.CheckCircle, OakCaughtGreen, "Caught")
        CollectionStatus.SEEN -> Triple(Icons.Filled.Visibility, OakSeenBlue, "Seen")
        CollectionStatus.NOT_CAUGHT -> Triple(
            Icons.Outlined.CheckCircleOutline,
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            "Not caught",
        )
    }
    IconButton(onClick = onCycleStatus, modifier = modifier.size(32.dp)) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
    }
}

/** Neutral filter chip: selected state is an ink fill rather than a tonal brand color. */
@Composable
fun OakFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = { Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    )
}
