package com.oaklog.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.PokemonSummary

/** Grid cell: the Graphite/Porcelain tile. */
@Composable
fun PokemonGridCard(
    pokemon: PokemonSummary,
    collectionEntry: CollectionEntry?,
    onClick: () -> Unit,
    onCycleStatus: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) = SleekTile(pokemon, collectionEntry, onClick, onCycleStatus, onToggleFavorite, modifier)

/** List row with a hairline divider. */
@Composable
fun PokemonListCard(
    pokemon: PokemonSummary,
    collectionEntry: CollectionEntry?,
    onClick: () -> Unit,
    onCycleStatus: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) = SleekRow(pokemon, collectionEntry, onClick, onCycleStatus, onToggleFavorite, modifier)
