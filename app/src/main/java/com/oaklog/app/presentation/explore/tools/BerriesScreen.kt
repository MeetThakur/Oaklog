package com.oaklog.app.presentation.explore.tools

import com.oaklog.app.presentation.components.OakIcons
import com.oaklog.app.presentation.components.DatabasePage
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.oaklog.app.domain.logic.BerryFilter
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.BerryInfo
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.ErrorView
import com.oaklog.app.presentation.components.FilterChip
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.components.TypeBadge
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.ChipShape
import com.oaklog.app.presentation.theme.typeTint

/** Each flavor borrows a type color purely as a hue, so the chips are easy to tell apart. */
private fun flavorColor(flavor: String) = when (flavor) {
    "spicy" -> "fire"
    "dry" -> "water"
    "sweet" -> "fairy"
    "bitter" -> "grass"
    "sour" -> "electric"
    else -> "normal"
}

@Composable
fun BerriesScreen(
    viewModel: BerriesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val flavors = remember(state.berries) { BerryFilter.flavors(state.berries) }
    val shown = remember(state.berries, state.flavor, state.query) {
        BerryFilter.filter(state.berries, state.flavor, state.query)
    }

    DatabasePage(
        title = "Berries",
        subtitle = "Flavors, growth and Natural Gift",
        color = "bug",
        icon = OakIcons.Berry,
        onNavigateBack = onNavigateBack,
        compact = true,
        modifier = modifier,
    ) {
        when {
            state.isLoading -> Column(
                Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Loading berries", style = MaterialTheme.typography.titleLarge)
                Text(
                    state.progress?.let { (d, t) -> "$d of $t. This only happens once." } ?: "Starting…",
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
                message = "Couldn't load the berries. Check your connection and try again.",
                onRetry = viewModel::load,
                modifier = Modifier,
            )
            else -> Column(Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::setQuery,
                    placeholder = { Text("Search ${state.berries.size} berries") },
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
                    FilterChip(selected = state.flavor == null, onClick = { viewModel.setFlavor(null) }, label = { Text("All") })
                    flavors.forEach { f ->
                        FilterChip(
                            selected = state.flavor == f,
                            onClick = { viewModel.setFlavor(if (state.flavor == f) null else f) },
                            label = { Text(NameFormatter.pretty(f)) },
                        )
                    }
                }
                Text(
                    if (state.flavor == null) "${shown.size} berries" else "${shown.size} berries, strongest ${state.flavor} first",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                if (shown.isEmpty()) {
                    EmptyView(
                        title = "No berry matches",
                        subtitle = "Try another flavor, or clear the search.",
                        icon = Icons.Outlined.SearchOff,
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(shown, key = { it.id }) { BerryCard(it) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BerryCard(berry: BerryInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/${berry.itemName}.png",
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(Modifier.weight(1f)) {
                Text(NameFormatter.pretty(berry.itemName), style = MaterialTheme.typography.titleMedium)
                Text(
                    "${berry.size / 10.0} cm · ${berry.firmness?.let { NameFormatter.pretty(it) } ?: "Unknown"} · Smoothness ${berry.smoothness}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            berry.naturalGiftType?.let { TypeBadge(type = it, small = true) }
        }

        if (berry.flavors.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                berry.flavors.forEach { f ->
                    val tint = typeTint(flavorColor(f.name))
                    Text(
                        "${NameFormatter.pretty(f.name)} ${f.potency}",
                        style = MaterialTheme.typography.labelMedium,
                        color = tint.onContainer,
                        modifier = Modifier.clip(ChipShape).background(tint.container).padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Fact("Ripens in", "${BerryFilter.hoursToRipen(berry)} h")
            Fact("Harvest", "up to ${berry.maxHarvest}")
            Fact("Natural Gift", if (berry.naturalGiftPower > 0) "${berry.naturalGiftPower} power" else "—")
        }
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}
