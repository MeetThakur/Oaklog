package com.oaklog.app.presentation.explore

import com.oaklog.app.presentation.components.SleekHeader
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.oaklog.app.presentation.components.OakIcons
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.ChipShape
import com.oaklog.app.presentation.theme.typeTint

private data class Tool(
    val title: String,
    val description: String,
    val icon: ImageVector,
    /** A type name used purely as a color: each tool gets a deliberate tint, not a meaning. */
    val color: String,
    val onClick: () -> Unit,
)

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToCollection: () -> Unit,
    onNavigateToTypeChart: () -> Unit,
    onNavigateToMoves: () -> Unit,
    onNavigateToAbilities: () -> Unit,
    onNavigateToItems: () -> Unit,
    onNavigateToRegions: () -> Unit,
    onNavigateToEvolutions: () -> Unit,
    onNavigateToNatures: () -> Unit,
    onNavigateToBerries: () -> Unit,
    onNavigateToBalls: () -> Unit,
    onNavigateToCompare: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tools = listOf(
        Tool("Collection", "Pokédex tracker & catches.", Icons.Outlined.BookmarkBorder, "dragon", onNavigateToCollection),
        Tool("Compare", "Two Pokémon side by side.", Icons.AutoMirrored.Filled.CompareArrows, "flying", onNavigateToCompare),
        Tool("Calculator", "Stats and damage.", Icons.Outlined.Calculate, "electric", onNavigateToCalculator),
        Tool("Type chart", "Damage multipliers.", Icons.Outlined.FlashOn, "psychic", onNavigateToTypeChart),
        Tool("Moves", "Power, accuracy, effects.", Icons.Outlined.AutoAwesome, "fire", onNavigateToMoves),
        Tool("Abilities", "Effects and who has them.", Icons.Outlined.Psychology, "grass", onNavigateToAbilities),
        Tool("Items", "Balls, berries, held items.", Icons.Outlined.Inventory2, "water", onNavigateToItems),
        Tool("Regions", "Places and wild Pokémon.", Icons.Outlined.Map, "ground", onNavigateToRegions),
        Tool("Poké Balls", "Catch multipliers and effects.", OakIcons.PokeBall, "poison", onNavigateToBalls),
        Tool("Berries", "Flavors, growth and Natural Gift.", OakIcons.Berry, "bug", onNavigateToBerries),
        Tool("Natures", "What each raises and lowers.", Icons.Outlined.Tune, "dragon", onNavigateToNatures),
        Tool("Evolutions", "What evolves how and with what.", Icons.AutoMirrored.Filled.TrendingUp, "ghost", onNavigateToEvolutions),
    )

    Scaffold(
        topBar = { SleekHeader(overline = "EXPLORE", title = "Research hub") },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SearchEntry(onClick = onNavigateToSearch)

            // Two tiles per row; an odd last tile spans the full width.
            tools.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { tool -> ToolTile(tool, Modifier.weight(1f)) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Looks like a search field but opens the full search screen, which owns the keyboard. */
@Composable
private fun SearchEntry(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ChipShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Search Pokémon, moves, abilities, items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ToolTile(tool: Tool, modifier: Modifier = Modifier) {
    val tint = typeTint(tool.color)
    val shape = CardShape
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = tool.onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(tint.container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(tool.icon, contentDescription = null, tint = tint.onContainer, modifier = Modifier.size(22.dp))
        }
        Column {
            Text(tool.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                tool.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
