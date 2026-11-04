package com.oaklog.app.presentation.explore.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.ItemInfo
import com.oaklog.app.presentation.components.DatabaseListPadding
import com.oaklog.app.presentation.components.DatabasePage
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.FilterChip
import com.oaklog.app.presentation.components.LoadingView
import com.oaklog.app.presentation.components.ReferenceSheet
import com.oaklog.app.presentation.components.ReferenceTarget
import com.oaklog.app.presentation.components.TintPill
import com.oaklog.app.presentation.components.TintedCard
import com.oaklog.app.presentation.theme.typeTint

/** A stable hue per category, so items of one kind share a color without a hand-kept table. */
private val CATEGORY_HUES = listOf("water", "grass", "fire", "electric", "psychic", "ground", "fairy", "dragon", "flying", "ghost")

private fun categoryHue(category: String?): String =
    CATEGORY_HUES[((category ?: "").hashCode().and(0x7fffffff)) % CATEGORY_HUES.size]

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemsDatabaseScreen(
    items: List<ItemInfo>,
    heldItems: List<ItemInfo>,
    isLoading: Boolean,
    onSearch: (String) -> Unit,
    onInit: () -> Unit,
    onLoadHeld: () -> Unit,
    onNavigateBack: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sheet by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var heldOnly by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { onInit() }

    // In "Held items" the list is complete, so searching filters it instead of asking the API.
    val shown = remember(heldOnly, query, items, heldItems) {
        if (!heldOnly) items
        else {
            val q = query.trim().lowercase().replace('-', ' ')
            if (q.isEmpty()) heldItems else heldItems.filter { it.name.replace('-', ' ').contains(q) }
        }
    }

    DatabasePage(
        title = "Items",
        subtitle = if (heldOnly) "${shown.size} held items" else "Balls, medicine, held items and more",
        color = "water",
        icon = Icons.Default.Inventory2,
        onNavigateBack = onNavigateBack,
        query = query,
        onQueryChange = { query = it; if (!heldOnly) onSearch(it) },
        searchHint = if (heldOnly) "Search held items" else "Search any item",
        modifier = modifier,
        chips = {
            FilterChip(selected = !heldOnly, onClick = { heldOnly = false }, label = { Text("All items") })
            FilterChip(
                selected = heldOnly,
                onClick = { heldOnly = true; onLoadHeld() },
                label = { Text("Held items") },
            )
        },
    ) {
        when {
            isLoading && shown.isEmpty() -> LoadingView(message = "Loading items...")
            shown.isEmpty() -> EmptyView(
                title = "No item found",
                subtitle = if (heldOnly) "No held item matches that search." else "Type a full item name, like leftovers or max potion.",
                icon = Icons.Outlined.SearchOff,
            )
            else -> LazyColumn(
                contentPadding = DatabaseListPadding,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(shown, key = { it.id }) { item -> ItemCard(item) { sheet = item.name } }
            }
        }
    }

    sheet?.let { name ->
        ReferenceSheet(
            target = ReferenceTarget.Item(name),
            onDismiss = { sheet = null },
            onOpenPokemon = onPokemonClick,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemCard(item: ItemInfo, onClick: () -> Unit) {
    val tint = typeTint(categoryHue(item.category))
    val holders = item.heldBy.filter { it.speciesId != null }
    TintedCard(tint, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier.size(60.dp).clip(CircleShape).background(tint.art),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = item.spriteUrl,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(NameFormatter.pretty(item.name), style = MaterialTheme.typography.titleMedium, color = tint.onContainer)
                item.category?.let {
                    Text(
                        NameFormatter.pretty(it),
                        style = MaterialTheme.typography.labelMedium,
                        color = tint.onContainer.copy(alpha = 0.75f),
                    )
                }
            }
        }
        item.description?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = tint.onContainer.copy(alpha = 0.9f))
        }
        val cost = item.cost?.takeIf { it > 0 }
        if (cost != null || holders.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                cost?.let { TintPill("₽$it", tint) }
                if (holders.isNotEmpty()) {
                    TintPill(
                        "Held by " + holders.take(2).joinToString(", ") { NameFormatter.pretty(it.pokemonName) } +
                            if (holders.size > 2) " +${holders.size - 2}" else "",
                        tint,
                    )
                }
            }
        }
    }
}
