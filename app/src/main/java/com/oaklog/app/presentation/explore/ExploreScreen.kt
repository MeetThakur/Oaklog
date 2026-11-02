package com.oaklog.app.presentation.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
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
    onNavigateToTypeChart: () -> Unit,
    onNavigateToMoves: () -> Unit,
    onNavigateToAbilities: () -> Unit,
    onNavigateToItems: () -> Unit,
    onNavigateToRegions: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Oak's Research Archives",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Explore the deep computational data, move registries, item inventories, and type interactions of the Pokémon world.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(6.dp))

            ExploreToolCard(
                title = "Interactive Type Chart",
                description = "Calculate damage multipliers, weaknesses, quad resistances, and immunities for any single or dual-type combination.",
                icon = Icons.Default.FlashOn,
                iconColor = Color(0xFFFBC02D),
                onClick = onNavigateToTypeChart,
            )

            ExploreToolCard(
                title = "Moves Database",
                description = "Search all Pokémon moves, inspect base power, accuracy, PP, damage categories, and battle effects.",
                icon = Icons.Default.AutoAwesome,
                iconColor = Color(0xFFE53935),
                onClick = onNavigateToMoves,
            )

            ExploreToolCard(
                title = "Abilities Database",
                description = "Look up in-depth passive ability effects and view all species and hidden ability bearers across generations.",
                icon = Icons.Default.Psychology,
                iconColor = Color(0xFF8E24AA),
                onClick = onNavigateToAbilities,
            )

            ExploreToolCard(
                title = "Items & Medicine Database",
                description = "Browse Poké Balls, held items, battle items, evolution stones, berries, and regional recovery medicine.",
                icon = Icons.Default.Inventory2,
                iconColor = Color(0xFF1E88E5),
                onClick = onNavigateToItems,
            )

            ExploreToolCard(
                title = "Regions & Generations",
                description = "Explore Pokémon regions from Kanto to Paldea, their regional Pokédex rosters, and game version groups.",
                icon = Icons.Default.Map,
                iconColor = Color(0xFF43A047),
                onClick = onNavigateToRegions,
            )
        }
    }
}

@Composable
private fun ExploreToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
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
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
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
