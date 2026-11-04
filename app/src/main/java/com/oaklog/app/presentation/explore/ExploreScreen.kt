package com.oaklog.app.presentation.explore

import com.oaklog.app.presentation.components.Card
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oaklog.app.presentation.components.OakTopBar

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToTypeChart: () -> Unit,
    onNavigateToMoves: () -> Unit,
    onNavigateToAbilities: () -> Unit,
    onNavigateToItems: () -> Unit,
    onNavigateToRegions: () -> Unit,
    onNavigateToCompare: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            OakTopBar(title = "Research Hub")
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SearchEntry(onClick = onNavigateToSearch)

            ExploreToolCard(
                title = "Compare Pokémon",
                description = "Stats, typing, size and abilities side by side.",
                icon = Icons.Default.CompareArrows,
                onClick = onNavigateToCompare,
            )

            ExploreToolCard(
                title = "Stat & Damage Calculator",
                description = "Stats by level, nature, IVs and EVs, plus damage estimates.",
                icon = Icons.Default.Calculate,
                onClick = onNavigateToCalculator,
            )

            ExploreToolCard(
                title = "Interactive Type Chart",
                description = "Damage multipliers for any single or dual typing.",
                icon = Icons.Default.FlashOn,
                onClick = onNavigateToTypeChart,
            )

            ExploreToolCard(
                title = "Moves Database",
                description = "Power, accuracy, PP, category and effect.",
                icon = Icons.Default.AutoAwesome,
                onClick = onNavigateToMoves,
            )

            ExploreToolCard(
                title = "Abilities Database",
                description = "Ability effects and which species carry them.",
                icon = Icons.Default.Psychology,
                onClick = onNavigateToAbilities,
            )

            ExploreToolCard(
                title = "Items Database",
                description = "Poké Balls, held items, stones, berries and medicine.",
                icon = Icons.Default.Inventory2,
                onClick = onNavigateToItems,
            )

            ExploreToolCard(
                title = "Regions & Generations",
                description = "Kanto to Paldea, with regional Pokédex rosters.",
                icon = Icons.Default.Map,
                onClick = onNavigateToRegions,
            )
        }
    }
}

/** Looks like a search field but opens the full search screen, which owns the keyboard. */
@Composable
private fun SearchEntry(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Search Pokémon, moves, abilities, items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExploreToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

