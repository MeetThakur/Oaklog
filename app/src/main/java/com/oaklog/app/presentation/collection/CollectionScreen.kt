package com.oaklog.app.presentation.collection

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.CatchingPokemon
import androidx.compose.material.icons.Icons
import com.oaklog.app.presentation.components.Card
import com.oaklog.app.presentation.components.FilterChip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.components.PokemonGridCard
import com.oaklog.app.presentation.theme.OakCaughtGreen
import com.oaklog.app.presentation.theme.OakFavoriteRed
import com.oaklog.app.presentation.theme.OakSeenBlue
import com.oaklog.app.presentation.theme.OakShinyGold

@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel,
    onPokemonClick: (Int) -> Unit,
    onBrowsePokedex: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            OakTopBar(title = "Research Collection")
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Progress Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Pokédex Completion",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${state.summary.caught.done} of ${state.summary.caught.total} Species",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Text(
                            text = "${state.summary.caught.percent}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { state.summary.caught.fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    Spacer(Modifier.height(14.dp))

                    // 3 Metric Badges: Seen, Shinies, Favorites
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        CollectionMetric(
                            label = "Seen",
                            count = state.summary.seen,
                            color = OakSeenBlue,
                        )
                        CollectionMetric(
                            label = "Shinies",
                            count = state.summary.shiny,
                            color = OakShinyGold,
                        )
                        CollectionMetric(
                            label = "Favorites",
                            count = state.summary.favorites,
                            color = OakFavoriteRed,
                        )
                    }
                }
            }

            // Generation Progress Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.selectedGen == null,
                    onClick = { viewModel.selectGen(null) },
                    label = { Text("All Gens") },
                )
                (1..9).forEach { gen ->
                    val p = state.summary.byGeneration[gen]
                    val pct = p?.percent ?: 0
                    FilterChip(
                        selected = state.selectedGen == gen,
                        onClick = { viewModel.selectGen(gen) },
                        label = { Text("Gen $gen ($pct%)") },
                    )
                }
            }

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CollectionFilterTab.entries.forEach { tab ->
                    FilterChip(
                        selected = state.activeTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        label = {
                            Text(when (tab) {
                                CollectionFilterTab.ALL -> "All"
                                CollectionFilterTab.CAUGHT -> "Caught"
                                CollectionFilterTab.SEEN -> "Seen"
                                CollectionFilterTab.UNCAUGHT -> "Uncaught"
                                CollectionFilterTab.SHINIES -> "Shinies"
                                CollectionFilterTab.FAVORITES -> "Favorites"
                            })
                        }
                    )
                }
            }

            // Grid of Pokémon
            Box(modifier = Modifier.weight(1f)) {
                if (state.displayList.isEmpty()) {
                    val empty = emptyContent(state.activeTab, state.selectedGen)
                    EmptyView(
                        title = empty.title,
                        subtitle = empty.subtitle,
                        icon = empty.icon,
                        actionLabel = if (empty.browse) "Browse the Pokédex" else null,
                        onAction = if (empty.browse) onBrowsePokedex else null,
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.displayList, key = { it.id }) { pokemon ->
                            PokemonGridCard(
                                pokemon = pokemon,
                                collectionEntry = state.collectionMap[pokemon.id],
                                onClick = { onPokemonClick(pokemon.id) },
                                onCycleStatus = { viewModel.cycleStatus(pokemon.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(pokemon.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionMetric(
    label: String,
    count: Int,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


private data class EmptyContent(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    /** Whether the Pokédex is where the user fixes this (as opposed to it being a good thing). */
    val browse: Boolean = true,
)

/** What an empty tab means differs: "Uncaught" being empty is an achievement, not a gap. */
private fun emptyContent(tab: CollectionFilterTab, gen: Int?): EmptyContent {
    val scope = gen?.let { " in Generation $it" }.orEmpty()
    return when (tab) {
        CollectionFilterTab.CAUGHT -> EmptyContent(
            "Nothing caught$scope yet",
            "Mark a Pokémon as caught from its card or detail page and it will appear here.",
            Icons.Outlined.CatchingPokemon,
        )
        CollectionFilterTab.SEEN -> EmptyContent(
            "No sightings logged$scope",
            "Tap the status dot on a Pokémon to mark it as seen.",
            Icons.Outlined.Visibility,
        )
        CollectionFilterTab.UNCAUGHT -> EmptyContent(
            "All caught$scope",
            "There's nothing left to catch here. Nicely done.",
            Icons.Outlined.EmojiEvents,
            browse = false,
        )
        CollectionFilterTab.SHINIES -> EmptyContent(
            "No shinies$scope yet",
            "Shiny catches you log will be collected here.",
            Icons.Outlined.AutoAwesome,
        )
        CollectionFilterTab.FAVORITES -> EmptyContent(
            "No favorites$scope yet",
            "Tap the heart on any Pokémon to keep it close.",
            Icons.Outlined.FavoriteBorder,
        )
        CollectionFilterTab.ALL -> EmptyContent(
            "Nothing here$scope",
            "Try another generation, or browse the Pokédex.",
            Icons.Outlined.CatchingPokemon,
        )
    }
}
