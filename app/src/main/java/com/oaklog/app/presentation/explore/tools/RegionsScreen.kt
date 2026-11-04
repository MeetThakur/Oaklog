package com.oaklog.app.presentation.explore.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.GenerationInfo
import com.oaklog.app.domain.model.RegionInfo
import com.oaklog.app.presentation.components.DatabaseListPadding
import com.oaklog.app.presentation.components.DatabasePage
import com.oaklog.app.presentation.components.TintPill
import com.oaklog.app.presentation.components.TintedCard
import com.oaklog.app.presentation.theme.typeTint

/** One hue per region, repeating, so neighbours in the list never share a color. */
private val REGION_HUES = listOf("grass", "electric", "water", "ice", "dark", "fairy", "fire", "ghost", "dragon")

/** PokéAPI slugs like "letsgo-kanto" read better as "Let's Go Kanto". */
private fun dexLabel(slug: String): String =
    NameFormatter.pretty(slug).replace("Letsgo", "Let's Go")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegionsScreen(
    regions: List<RegionInfo>,
    generations: List<GenerationInfo>,
    onNavigateBack: () -> Unit,
    onRegionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    DatabasePage(
        title = "Regions",
        subtitle = "${regions.size} regions · tap one for its places and wild Pokémon",
        color = "ground",
        icon = Icons.Outlined.Map,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    ) {
        LazyColumn(
            contentPadding = DatabaseListPadding,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(regions, key = { it.id }) { region ->
                val tint = typeTint(REGION_HUES[(region.id - 1).mod(REGION_HUES.size)])
                TintedCard(tint, onClick = { onRegionClick(region.name) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            NameFormatter.pretty(region.name),
                            style = MaterialTheme.typography.titleLarge,
                            color = tint.onContainer,
                            modifier = Modifier.weight(1f),
                        )
                        region.generation?.let { TintPill("Gen ${NameFormatter.generationRoman(it)}", tint) }
                    }
                    if (region.pokedexes.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            region.pokedexes.forEach { TintPill(dexLabel(it), tint) }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Places and wild Pokémon",
                            style = MaterialTheme.typography.labelLarge,
                            color = tint.onContainer,
                        )
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = tint.onContainer)
                    }
                }
            }

            if (generations.isNotEmpty()) {
                item {
                    Text(
                        "Generations",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 14.dp, bottom = 2.dp),
                    )
                }
                items(generations, key = { "gen${it.id}" }) { gen ->
                    val tint = typeTint(REGION_HUES[(gen.id - 1).mod(REGION_HUES.size)])
                    TintedCard(tint) {
                        Text(NameFormatter.pretty(gen.name), style = MaterialTheme.typography.titleMedium, color = tint.onContainer)
                        gen.region?.let {
                            Text(
                                "Main region: ${NameFormatter.pretty(it)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = tint.onContainer.copy(alpha = 0.85f),
                            )
                        }
                        if (gen.versionGroups.isNotEmpty()) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                gen.versionGroups.forEach { TintPill(NameFormatter.pretty(it), tint) }
                            }
                        }
                    }
                }
            }
        }
    }
}
