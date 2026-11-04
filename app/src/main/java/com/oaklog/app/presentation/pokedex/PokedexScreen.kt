package com.oaklog.app.presentation.pokedex

import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.oaklog.app.domain.logic.CaughtFilter
import com.oaklog.app.domain.model.DexLayout
import com.oaklog.app.domain.model.ThemeMode
import com.oaklog.app.presentation.components.*

@Composable
fun PokedexScreen(
    viewModel: PokedexViewModel,
    onPokemonClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PokedexContent(viewModel, onPokemonClick, onSettingsClick, modifier)
}

@Composable
private fun PokedexContent(
    viewModel: PokedexViewModel,
    onPokemonClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SleekHeader(
                overline = "POKÉDEX",
                title = "All species",
                actions = {
                    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                    IconButton(
                        onClick = { viewModel.setThemeMode(if (isDark) ThemeMode.LIGHT else ThemeMode.DARK) },
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                        )
                    }
                }
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Filter controls
            OakSearchBar(
                query = state.filters.query,
                onQueryChange = viewModel::setQuery,
                onFilterClick = { showFilterSheet = true },
                activeFilterCount = state.filters.activeCount,
                layout = state.layout,
                onToggleLayout = viewModel::toggleLayout,
            )

            // Quick filter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OakFilterChip(
                    selected = state.filters.caught == CaughtFilter.CAUGHT,
                    onClick = {
                        val next = if (state.filters.caught == CaughtFilter.CAUGHT) CaughtFilter.ANY else CaughtFilter.CAUGHT
                        viewModel.updateFilters(state.filters.copy(caught = next))
                    },
                    label = "Caught",
                )
                OakFilterChip(
                    selected = state.filters.favoritesOnly,
                    onClick = {
                        viewModel.updateFilters(state.filters.copy(favoritesOnly = !state.filters.favoritesOnly))
                    },
                    label = "Favorites",
                )
                OakFilterChip(
                    selected = state.filters.legendaryOnly,
                    onClick = {
                        viewModel.updateFilters(state.filters.copy(legendaryOnly = !state.filters.legendaryOnly))
                    },
                    label = "Legendary",
                )
                (1..9).forEach { gen ->
                    OakFilterChip(
                        selected = state.filters.generations.contains(gen),
                        onClick = { viewModel.toggleGenFilter(gen) },
                        label = "Gen $gen",
                    )
                }
            }

            // Results count banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${state.pokemonList.size} Pokémon",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.filters.activeCount > 0) {
                    TextButton(
                        onClick = viewModel::resetFilters,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text("Clear filters", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            state.indexProgress?.let { progress ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Indexing details for filters and sorting · ${progress.done} / ${progress.total}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { progress.done.toFloat() / progress.total.coerceAtLeast(1) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface,
                        trackColor = MaterialTheme.colorScheme.outline,
                        gapSize = 0.dp,
                        drawStopIndicator = {},
                    )
                }
            }

            // Main List / Grid
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading && state.pokemonList.isEmpty() -> {
                        LoadingView(message = "Synchronizing Professor Oak's research index...")
                    }
                    state.indexProgress != null && state.pokemonList.isEmpty() -> {
                        LoadingView(message = "Matches appear as details are indexed...")
                    }
                    state.error != null && state.pokemonList.isEmpty() -> {
                        ErrorView(
                            message = state.error!!,
                            onRetry = { viewModel.sync(force = true) }
                        )
                    }
                    state.pokemonList.isEmpty() -> {
                        EmptyView(
                            title = "No Pokémon found",
                            subtitle = "Nothing matches the current search and filters. Loosen one of them to see more species.",
                            icon = Icons.Outlined.SearchOff,
                        )
                    }
                    state.layout == DexLayout.GRID -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(state.pokemonList, key = { it.id }) { pokemon ->
                                SleekTile(
                                    pokemon = pokemon,
                                    collectionEntry = state.collectionMap[pokemon.id],
                                    onClick = { onPokemonClick(pokemon.id) },
                                    onCycleStatus = { viewModel.cycleStatus(pokemon.id) },
                                    onToggleFavorite = { viewModel.toggleFavorite(pokemon.id) },
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(state.pokemonList, key = { it.id }) { pokemon ->
                                SleekRow(
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

    if (showFilterSheet) {
        PokedexFilterBottomSheet(
            filters = state.filters,
            onApply = viewModel::updateFilters,
            onReset = viewModel::resetFilters,
            onDismiss = { showFilterSheet = false },
        )
    }
}

