package com.oaklog.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.PokemonSummary

/** Grid cell: a full [TypeCard]. */
@Composable
fun PokemonGridCard(
    pokemon: PokemonSummary,
    collectionEntry: CollectionEntry?,
    onClick: () -> Unit,
    onCycleStatus: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) = TypeCard(pokemon, collectionEntry, onClick, onCycleStatus, onToggleFavorite, modifier)

/** List row: the same card, shorter, with type pills in a row. */
@Composable
fun PokemonListCard(
    pokemon: PokemonSummary,
    collectionEntry: CollectionEntry?,
    onClick: () -> Unit,
    onCycleStatus: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) = TypeCard(pokemon, collectionEntry, onClick, onCycleStatus, onToggleFavorite, modifier, compact = true)
