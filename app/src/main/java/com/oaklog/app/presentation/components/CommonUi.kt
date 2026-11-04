package com.oaklog.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
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
import com.oaklog.app.presentation.theme.ChipShape
import com.oaklog.app.presentation.theme.OakCaughtGreen
import com.oaklog.app.presentation.theme.TypeTint
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

/** Detail-header status control: a pill that cycles uncaught, seen, caught. Tinted on a type header. */
@Composable
fun CollectionStatusButton(
    entry: CollectionEntry?,
    onCycleStatus: () -> Unit,
    modifier: Modifier = Modifier,
    tint: TypeTint? = null,
) {
    val status = entry?.status ?: CollectionStatus.NOT_CAUGHT
    val caught = status == CollectionStatus.CAUGHT
    val label = when (status) {
        CollectionStatus.CAUGHT -> "Caught"
        CollectionStatus.SEEN -> "Seen"
        CollectionStatus.NOT_CAUGHT -> "Not caught"
    }
    val icon = when (status) {
        CollectionStatus.CAUGHT -> Icons.Filled.Check
        CollectionStatus.SEEN -> Icons.Filled.Visibility
        CollectionStatus.NOT_CAUGHT -> Icons.Filled.Add
    }
    val ink = tint?.onContainer ?: MaterialTheme.colorScheme.onSurface
    val background = when {
        caught -> ink
        else -> tint?.chip ?: MaterialTheme.colorScheme.surfaceVariant
    }
    val foreground = if (caught) (tint?.container ?: MaterialTheme.colorScheme.background) else ink

    Row(
        modifier = modifier
            .clip(ChipShape)
            .background(background)
            .clickable { onCycleStatus() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = foreground, modifier = Modifier.size(16.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = foreground)
    }
}

@Composable
fun FavoriteIconButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    tint: TypeTint? = null,
) {
    // On a tinted card the idle heart takes the card's text color so it stays legible in both modes.
    val idle = tint?.onContainer?.copy(alpha = 0.75f)
        ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    IconButton(
        onClick = onToggle,
        modifier = modifier.size(32.dp),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) OakFavoriteRed else idle,
            modifier = Modifier.size(19.dp),
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
        shape = ChipShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = androidx.compose.ui.graphics.Color.Transparent,
            selectedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
    )
}
