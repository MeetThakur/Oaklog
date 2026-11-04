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
import androidx.compose.foundation.layout.fillMaxHeight
import com.oaklog.app.presentation.theme.typeTint
import com.oaklog.app.presentation.theme.ChipShape
import com.oaklog.app.presentation.theme.CardShape

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
            // Completion header: the headline number, a thick progress bar, three tinted tallies.
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Pokédex completion",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${state.summary.caught.percent}%",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = "${state.summary.caught.done} of ${state.summary.caught.total} caught",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(ChipShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(state.summary.caught.fraction.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(ChipShape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Tally("Seen", state.summary.seen, "water", Modifier.weight(1f))
                    Tally("Shinies", state.summary.shiny, "electric", Modifier.weight(1f))
                    Tally("Favorites", state.summary.favorites, "fairy", Modifier.weight(1f))
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
private fun Tally(label: String, count: Int, color: String, modifier: Modifier = Modifier) {
    val tint = typeTint(color)
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(tint.container)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(count.toString(), style = MaterialTheme.typography.headlineSmall, color = tint.onContainer)
        Text(label, style = MaterialTheme.typography.labelMedium, color = tint.onContainer.copy(alpha = 0.75f))
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
