package com.oaklog.app.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.NamedEntry
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.ReferenceSheet
import com.oaklog.app.presentation.components.ReferenceTarget
import com.oaklog.app.presentation.components.TypeBadge

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val focus = remember { FocusRequester() }
    var sheet by remember { mutableStateOf<ReferenceTarget?>(null) }

    LaunchedEffect(Unit) { focus.requestFocus() }

    Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.weight(1f).focusRequester(focus),
                    placeholder = { Text("Pokémon, moves, abilities, items") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }

            val results = state.results
            when {
                state.query.isBlank() -> EmptyView(
                    title = "Search everything",
                    subtitle = "Find a Pokémon by name or number, or look up a move, ability or item.",
                    icon = Icons.Outlined.TravelExplore,
                )
                results.isEmpty && state.loadingNames -> EmptyView(
                    title = "Searching…",
                    subtitle = "Loading move, ability and item names.",
                    icon = Icons.Outlined.TravelExplore,
                )
                results.isEmpty -> EmptyView(
                    title = "No matches for \"${state.query.trim()}\"",
                    subtitle = "Check the spelling, or try part of the name.",
                    icon = Icons.Outlined.SearchOff,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (results.pokemon.isNotEmpty()) {
                        item { SectionHeader("Pokémon") }
                        items(results.pokemon, key = { "p${it.id}" }) { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPokemonClick(p.id) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                AsyncImage(
                                    model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${p.id}.png",
                                    contentDescription = null,
                                    modifier = Modifier.size(44.dp),
                                    contentScale = ContentScale.Fit,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        NameFormatter.dexNumber(p.id),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(p.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    p.types.forEach { TypeBadge(type = it, small = true) }
                                }
                            }
                        }
                    }
                    referenceSection("Moves", results.moves) { sheet = ReferenceTarget.Move(it) }
                    referenceSection("Abilities", results.abilities) { sheet = ReferenceTarget.Ability(it) }
                    referenceSection("Items", results.items) { sheet = ReferenceTarget.Item(it) }
                }
            }
        }
    }

    sheet?.let { target ->
        ReferenceSheet(
            target = target,
            onDismiss = { sheet = null },
            onOpenPokemon = onPokemonClick,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.referenceSection(
    title: String,
    entries: List<NamedEntry>,
    onClick: (String) -> Unit,
) {
    if (entries.isEmpty()) return
    item { SectionHeader(title) }
    items(entries, key = { "$title${it.id}" }) { e ->
        Text(
            text = NameFormatter.pretty(e.name),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(e.name) }
                .padding(vertical = 12.dp),
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}
