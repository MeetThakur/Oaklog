package com.oaklog.app.presentation.places

import androidx.compose.material.icons.outlined.Map
import com.oaklog.app.presentation.components.DatabasePage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.logic.Places
import com.oaklog.app.domain.model.AreaSpawn
import com.oaklog.app.domain.model.LocationSummary
import com.oaklog.app.domain.model.SummaryMon
import com.oaklog.app.domain.model.SpawnEntry
import com.oaklog.app.presentation.components.DropdownOption
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.ErrorView
import com.oaklog.app.presentation.components.LoadingView
import com.oaklog.app.presentation.components.OakDropdown
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.ChipShape

/** Every location in a region, searchable. */
@Composable
fun RegionPlacesScreen(
    viewModel: RegionPlacesViewModel,
    onNavigateBack: () -> Unit,
    onLocationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val summaries by viewModel.summaries.collectAsState()
    var query by remember { mutableStateOf("") }
    var game by remember { mutableStateOf("") }
    val region = viewModel.region
    // Games offered are those seen so far, so the list grows as rows load their summaries.
    val games = remember(summaries) {
        summaries.values.flatMap { it.getOrNull()?.perGame?.keys.orEmpty() }.distinct().sorted()
    }
    val shown = remember(state.locations, query, game, summaries) {
        val q = query.trim().lowercase().replace('-', ' ')
        state.locations.filter { loc ->
            (q.isEmpty() || loc.name.replace('-', ' ').contains(q)) &&
                // With a game picked, hide places we know have nothing in it; unloaded ones stay.
                (game.isEmpty() || summaries[loc.name]?.getOrNull()?.forGame(game) != null || summaries[loc.name] == null)
        }
    }

    DatabasePage(
        title = NameFormatter.pretty(region),
        subtitle = "Places and wild Pokémon",
        color = "ground",
        icon = Icons.Outlined.Map,
        onNavigateBack = onNavigateBack,
        compact = true,
        modifier = modifier,
    ) {
        Column(Modifier.fillMaxSize()) {
            when {
                state.isLoading -> LoadingView(message = "Loading places...")
                state.failed -> ErrorView(message = "Couldn't load this region's places.", onRetry = viewModel::load)
                state.locations.isEmpty() -> EmptyView(
                    title = "No places listed",
                    subtitle = "PokéAPI has no locations recorded for this region.",
                    icon = Icons.Outlined.Place,
                )
                else -> {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search ${state.locations.size} places") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
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
                    if (games.isNotEmpty()) {
                        OakDropdown(
                            label = "Game",
                            options = listOf(DropdownOption("", "All games")) +
                                games.map { DropdownOption(it, NameFormatter.pretty(it)) },
                            selected = game,
                            onSelect = { game = it },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                    if (shown.isEmpty()) {
                        EmptyView(
                            title = "No place matches",
                            subtitle = "Nothing in ${NameFormatter.pretty(region)} matches \"${query.trim()}\".",
                            icon = Icons.Outlined.SearchOff,
                        )
                    } else {
                        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                            items(shown, key = { it.id }) { place ->
                                LocationRow(
                                    label = Places.locationLabel(place.name, region),
                                    summary = summaries[place.name],
                                    game = game.ifEmpty { null },
                                    onShow = { viewModel.requestSummary(place.name) },
                                    onRetry = { viewModel.retrySummary(place.name) },
                                    onClick = { onLocationClick(place.name) },
                                )
                                androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** One location: its areas, each opening to the wild Pokémon found there. */
@Composable
fun LocationScreen(
    viewModel: LocationViewModel,
    onNavigateBack: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val location = viewModel.location
    var game by remember { mutableStateOf("") }
    val games = remember(state.areas) {
        state.areas.flatMap { it.spawns.orEmpty() }.flatMap { it.entries }.map { it.version }.distinct().sorted()
    }

    DatabasePage(
        title = NameFormatter.pretty(location),
        subtitle = "Areas and wild Pokémon",
        color = "ground",
        icon = Icons.Outlined.Place,
        onNavigateBack = onNavigateBack,
        compact = true,
        modifier = modifier,
    ) {
        when {
            state.isLoading -> LoadingView(message = "Loading areas...", modifier = Modifier)
            state.failed -> ErrorView(
                message = "Couldn't load this location.",
                onRetry = viewModel::load,
                modifier = Modifier,
            )
            state.areas.isEmpty() -> EmptyView(
                title = "No areas listed",
                subtitle = "PokéAPI has no encounter areas for this location.",
                icon = Icons.Outlined.Place,
                modifier = Modifier,
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (games.isNotEmpty()) {
                    item {
                        OakDropdown(
                            label = "Game",
                            options = listOf(DropdownOption("", "All games")) +
                                games.map { DropdownOption(it, NameFormatter.pretty(it)) },
                            selected = game,
                            onSelect = { game = it },
                        )
                    }
                }
                items(state.areas, key = { it.area.id }) { area ->
                    AreaCard(
                        label = Places.areaLabel(area.area.name, location),
                        state = area,
                        game = game.ifEmpty { null },
                        onOpen = { viewModel.loadArea(area.area.name) },
                        onPokemonClick = onPokemonClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun AreaCard(
    label: String,
    state: AreaState,
    game: String?,
    onOpen: () -> Unit,
    onPokemonClick: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(state.spawns != null) }
    val spawns = state.spawns?.let { all ->
        if (game == null) all else all.filter { s -> s.entries.any { it.version == game } }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = !expanded
                    if (expanded) onOpen()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                if (spawns != null) {
                    Text(
                        "${spawns.size} Pokémon",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (expanded) {
            when {
                state.failed -> Note("Couldn't load this area. Collapse and open it again to retry.")
                spawns == null -> Note("Loading…")
                spawns.isEmpty() -> Note(
                    if (game == null) "No wild Pokémon are recorded here."
                    else "No wild Pokémon are recorded here for ${NameFormatter.pretty(game)}.",
                )
                else -> spawns.forEach { SpawnRow(it, game, onPokemonClick) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Note(text: String) = Text(
    text = text,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
)

@Composable
private fun SpawnRow(spawn: AreaSpawn, game: String?, onPokemonClick: (Int) -> Unit) {
    // With a game picked show its numbers; otherwise summarize across games.
    val entries: List<SpawnEntry> = if (game == null) spawn.entries else spawn.entries.filter { it.version == game }
    val min = entries.filter { it.minLevel > 0 }.minOfOrNull { it.minLevel } ?: 0
    val max = entries.maxOfOrNull { it.maxLevel } ?: 0
    val chance = entries.maxOfOrNull { it.chance } ?: 0
    val methods = entries.flatMap { it.methods }.distinct().joinToString(" · ") { Places.methodLabel(it) }
    val clickable = spawn.speciesId != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (clickable) it.clickable { onPokemonClick(spawn.speciesId!!) } else it }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            spawn.speciesId?.let { id ->
                AsyncImage(
                    model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png",
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                NameFormatter.pretty(spawn.pokemonName),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (game == null) {
                Text(
                    listOf(methods, "${entries.size} games").filter { it.isNotEmpty() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                // One game picked: show each way to meet it, with its own rate, levels and conditions.
                val slots = entries.flatMap { it.slots }
                if (slots.isEmpty()) {
                    Text(methods, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    slots.forEach { slot ->
                        Text(
                            Places.slotSummary(slot),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(Places.levelRange(min, max), style = MaterialTheme.typography.labelLarge)
            if (chance > 0) {
                Text("$chance%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * One location in a region: its name, then what lives there. The summary loads when the row first
 * appears, so a long region doesn't fetch every location up front.
 */
@Composable
private fun LocationRow(
    label: String,
    summary: Result<LocationSummary>?,
    game: String?,
    onShow: () -> Unit,
    onRetry: () -> Unit,
    onClick: () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(label) { onShow() }
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                val info = summary?.getOrNull()?.forGame(game)
                when {
                    summary == null -> Text("Loading…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    summary.isFailure -> Text(
                        "Couldn't load. Tap to retry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable(onClick = onRetry),
                    )
                    summary.getOrNull() != null && info == null -> Text(
                        "Not found here in ${NameFormatter.pretty(game ?: "")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    info != null && info.pokemon.isEmpty() -> Text(
                        "No wild Pokémon recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    info != null -> {
                        Text(
                            "${info.pokemon.size} Pokémon" +
                                if (info.maxLevel > 0) " · ${Places.levelRange(info.minLevel, info.maxLevel)}" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        MonStrip(info.pokemon)
                    }
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Up to six small sprites, then a "+N" for the rest. Forms without a species id are counted, not drawn. */
@Composable
private fun MonStrip(pokemon: List<SummaryMon>) {
    val shown = pokemon.filter { it.id != null }.take(6)
    if (shown.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp), verticalAlignment = Alignment.CenterVertically) {
        shown.forEach { mon ->
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${mon.id}.png",
                    contentDescription = NameFormatter.pretty(mon.name),
                    modifier = Modifier.size(30.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        val rest = pokemon.size - shown.size
        if (rest > 0) {
            Text(
                "+$rest",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}
