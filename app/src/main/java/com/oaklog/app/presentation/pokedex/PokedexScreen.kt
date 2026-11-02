package com.oaklog.app.presentation.pokedex

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
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oaklog.app.domain.logic.CaughtFilter
import com.oaklog.app.domain.model.DexLayout
import com.oaklog.app.presentation.components.*

@Composable
fun PokedexScreen(
    viewModel: PokedexViewModel,
    onPokemonClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            OakTopBar(
                title = "OakLog Pokédex",
                actions = {
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
                FilterChip(
                    selected = state.filters.caught == CaughtFilter.CAUGHT,
                    onClick = {
                        val next = if (state.filters.caught == CaughtFilter.CAUGHT) CaughtFilter.ANY else CaughtFilter.CAUGHT
                        viewModel.updateFilters(state.filters.copy(caught = next))
                    },
                    label = { Text("Caught") }
                )
                FilterChip(
                    selected = state.filters.favoritesOnly,
                    onClick = {
                        viewModel.updateFilters(state.filters.copy(favoritesOnly = !state.filters.favoritesOnly))
                    },
                    label = { Text("Favorites") }
                )
                FilterChip(
                    selected = state.filters.legendaryOnly,
                    onClick = {
                        viewModel.updateFilters(state.filters.copy(legendaryOnly = !state.filters.legendaryOnly))
                    },
                    label = { Text("Legendary") }
                )
                (1..9).forEach { gen ->
                    FilterChip(
                        selected = state.filters.generations.contains(gen),
                        onClick = { viewModel.toggleGenFilter(gen) },
                        label = { Text("Gen $gen") }
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

            // Main List / Grid
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading && state.pokemonList.isEmpty() -> {
                        LoadingView(message = "Synchronizing Professor Oak's research index...")
                    }
                    state.error != null && state.pokemonList.isEmpty() -> {
                        ErrorView(
                            message = state.error!!,
                            onRetry = { viewModel.sync(force = true) }
                        )
                    }
                    state.pokemonList.isEmpty() -> {
                        EmptyView(
                            title = "No Pokémon Found",
                            subtitle = "Try adjusting your search query or filters to explore more species."
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
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(state.pokemonList, key = { it.id }) { pokemon ->
                                PokemonListCard(
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
