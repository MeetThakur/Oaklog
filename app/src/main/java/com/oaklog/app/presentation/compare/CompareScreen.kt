package com.oaklog.app.presentation.compare

import androidx.compose.material.icons.automirrored.filled.CompareArrows
import com.oaklog.app.presentation.components.DatabasePage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.PokemonDetail
import com.oaklog.app.presentation.components.Card
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.components.TypeBadge
import com.oaklog.app.presentation.teams.PokemonPickerSheet
import com.oaklog.app.presentation.theme.PokemonTypeColors

@Composable
fun CompareScreen(
    viewModel: CompareViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var picking by remember { mutableStateOf<Side?>(null) }

    DatabasePage(
        title = "Compare",
        subtitle = "Two Pokémon side by side",
        color = "flying",
        icon = Icons.AutoMirrored.Filled.CompareArrows,
        onNavigateBack = onNavigateBack,
        compact = true,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Slot(state.a, state.loadingA, Modifier.weight(1f)) { picking = Side.A }
                Slot(state.b, state.loadingB, Modifier.weight(1f)) { picking = Side.B }
            }

            val a = state.a
            val b = state.b
            if (a == null || b == null) {
                Box(Modifier.fillMaxWidth().height(260.dp)) {
                    EmptyView(
                        title = "Pick two Pokémon",
                        subtitle = "Choose both slots to compare stats, typing, size and abilities side by side.",
                        icon = Icons.AutoMirrored.Outlined.CompareArrows,
                    )
                }
            } else {
                StatComparison(a, b)
                FactsComparison(a, b)
            }
        }
    }

    picking?.let { side ->
        PokemonPickerSheet(
            pokemonList = state.index,
            title = if (side == Side.A) "First Pokémon" else "Second Pokémon",
            onSelectPokemon = { viewModel.select(side, it.id) },
            onDismiss = { picking = null },
        )
    }
}

@Composable
private fun Slot(detail: PokemonDetail?, loading: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        ) {
            if (detail == null) {
                Box(
                    modifier = Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Choose Pokémon", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    if (loading) "Loading…" else "Choose",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val tint = PokemonTypeColors.color(detail.types.firstOrNull() ?: "unknown")
                Box(
                    modifier = Modifier.size(96.dp).clip(CircleShape).background(tint.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(detail.artworkUrl, detail.displayName, Modifier.size(84.dp), contentScale = ContentScale.Fit)
                }
                Text(NameFormatter.dexNumber(detail.speciesId), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    detail.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    detail.types.forEach { TypeBadge(type = it, small = true) }
                }
            }
        }
    }
}

@Composable
private fun StatComparison(a: PokemonDetail, b: PokemonDetail) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Base stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            a.stats.forEach { stat ->
                CompareRow(stat.label, stat.value, b.baseStat(stat.key), max = 160)
            }
            CompareRow("Total", a.baseStatTotal, b.baseStatTotal, max = 640, emphasize = true)
        }
    }
}

@Composable
private fun FactsComparison(a: PokemonDetail, b: PokemonDetail) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            FactRow("Height", "%.1f m".format(a.heightDm / 10.0), "%.1f m".format(b.heightDm / 10.0))
            FactRow("Weight", "%.1f kg".format(a.weightHg / 10.0), "%.1f kg".format(b.weightHg / 10.0))
            FactRow("Category", a.genus ?: "—", b.genus ?: "—")
            FactRow(
                "Abilities",
                a.abilities.joinToString("\n") { NameFormatter.pretty(it.name) + if (it.isHidden) " (H)" else "" },
                b.abilities.joinToString("\n") { NameFormatter.pretty(it.name) + if (it.isHidden) " (H)" else "" },
            )
        }
    }
}

/** One stat: both values, with the winner's bar and number in the primary color. */
@Composable
private fun CompareRow(label: String, left: Int, right: Int, max: Int, emphasize: Boolean = false) {
    val winner = when {
        left > right -> 1
        right > left -> 2
        else -> 0
    }
    val hot = MaterialTheme.colorScheme.primary
    val cold = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                left.toString(),
                modifier = Modifier.width(44.dp),
                fontWeight = if (winner == 1) FontWeight.Bold else FontWeight.Normal,
                color = if (winner == 1) hot else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                right.toString(),
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.End,
                fontWeight = if (winner == 2) FontWeight.Bold else FontWeight.Normal,
                color = if (winner == 2) hot else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Left bar grows leftwards from the centre line, right bar rightwards.
            Box(Modifier.weight(1f).height(6.dp), contentAlignment = Alignment.CenterEnd) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((left.toFloat() / max).coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (winner == 1) hot else cold),
                )
            }
            Box(Modifier.weight(1f).height(6.dp), contentAlignment = Alignment.CenterStart) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((right.toFloat() / max).coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (winner == 2) hot else cold),
                )
            }
        }
    }
}

@Composable
private fun FactRow(label: String, left: String, right: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row {
            Text(left, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(12.dp))
            Text(right, modifier = Modifier.weight(1f), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
