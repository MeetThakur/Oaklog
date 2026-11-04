package com.oaklog.app.presentation.explore.tools

import androidx.compose.material.icons.automirrored.filled.TrendingUp
import com.oaklog.app.presentation.components.DatabasePage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.EvolutionFinder
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.EvolutionStep
import com.oaklog.app.presentation.components.DropdownOption
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.ErrorView
import com.oaklog.app.presentation.components.FilterChip
import com.oaklog.app.presentation.components.OakDropdown
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.ChipShape

private fun art(id: Int) =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

/** "What evolves with a Fire Stone?" Filter every evolution by method, item, or Pokémon name. */
@Composable
fun EvolutionFinderScreen(
    viewModel: EvolutionFinderViewModel,
    onNavigateBack: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val methods = remember(state.steps) { EvolutionFinder.methods(state.steps) }
    val items = remember(state.steps, state.method) { EvolutionFinder.items(state.steps, state.method) }
    val results = remember(state.steps, state.method, state.item, state.query) {
        EvolutionFinder.filter(state.steps, state.method, state.item, state.query)
    }

    DatabasePage(
        title = "Evolutions",
        subtitle = "What evolves how and with what",
        color = "ghost",
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        onNavigateBack = onNavigateBack,
        compact = true,
        modifier = modifier,
    ) {
        when {
            state.isLoading -> Column(
                Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Indexing evolutions", style = MaterialTheme.typography.titleLarge)
                Text(
                    state.progress?.let { (done, total) -> "$done of $total chains. This only happens once." }
                        ?: "Starting…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )
                LinearProgressIndicator(
                    progress = { state.progress?.let { (d, t) -> d.toFloat() / t.coerceAtLeast(1) } ?: 0f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            state.failed -> ErrorView(
                message = "Couldn't load every evolution chain. Check your connection and try again.",
                onRetry = viewModel::load,
                modifier = Modifier,
            )
            else -> Column(Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::setQuery,
                    placeholder = { Text("Search a Pokémon") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    },
                    singleLine = true,
                    shape = ChipShape,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(selected = state.method == null, onClick = { viewModel.setMethod(null) }, label = { Text("All") })
                    methods.forEach { m ->
                        FilterChip(
                            selected = state.method == m,
                            onClick = { viewModel.setMethod(if (state.method == m) null else m) },
                            label = { Text(m.label) },
                        )
                    }
                }
                if (items.isNotEmpty()) {
                    OakDropdown(
                        label = "Item",
                        options = listOf(DropdownOption("", "Any item")) +
                            items.map { DropdownOption(it, NameFormatter.pretty(it)) },
                        selected = state.item ?: "",
                        onSelect = { viewModel.setItem(it.ifEmpty { null }) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                Text(
                    "${results.size} evolutions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                if (results.isEmpty()) {
                    EmptyView(
                        title = "No evolution matches",
                        subtitle = "Try a different item or method, or clear the search.",
                        icon = Icons.Outlined.SearchOff,
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(results, key = { "${it.fromId}-${it.toId}-${it.requirement}" }) { step ->
                            StepRow(step, onPokemonClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(step: EvolutionStep, onPokemonClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Species(step.fromId, step.fromName, onPokemonClick)
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "evolves into",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Species(step.toId, step.toName, onPokemonClick)
        }
        Text(
            step.requirement,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun Species(id: Int, name: String, onPokemonClick: (Int) -> Unit) {
    Row(
        modifier = Modifier.clip(CircleShape).clickable { onPokemonClick(id) }.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            AsyncImage(art(id), contentDescription = null, modifier = Modifier.size(44.dp), contentScale = ContentScale.Fit)
        }
        Text(NameFormatter.pretty(name), style = MaterialTheme.typography.titleSmall)
    }
}
