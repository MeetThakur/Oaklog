package com.oaklog.app.presentation.explore.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Psychology
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
import androidx.compose.ui.unit.dp
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.AbilityInfo
import com.oaklog.app.presentation.components.DatabaseListPadding
import com.oaklog.app.presentation.components.DatabasePage
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.LoadingView
import com.oaklog.app.presentation.components.ReferenceSheet
import com.oaklog.app.presentation.components.ReferenceTarget
import com.oaklog.app.presentation.components.TintPill
import com.oaklog.app.presentation.components.TintedCard
import com.oaklog.app.presentation.theme.typeTint

@Composable
fun AbilitiesDatabaseScreen(
    abilities: List<AbilityInfo>,
    isLoading: Boolean,
    onSearch: (String) -> Unit,
    onInit: () -> Unit,
    onNavigateBack: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var sheet by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { onInit() }

    DatabasePage(
        title = "Abilities",
        subtitle = if (abilities.isEmpty()) "What each does and who has it" else "${abilities.size} shown · tap one to see who has it",
        color = "grass",
        icon = Icons.Outlined.Psychology,
        onNavigateBack = onNavigateBack,
        query = query,
        onQueryChange = { query = it; onSearch(it) },
        searchHint = "Search abilities",
        modifier = modifier,
    ) {
        when {
            isLoading && abilities.isEmpty() -> LoadingView(message = "Searching abilities...")
            abilities.isEmpty() -> EmptyView(
                title = "No ability found",
                subtitle = "Type a full ability name, like intimidate or levitate.",
                icon = Icons.Outlined.SearchOff,
            )
            else -> LazyColumn(
                contentPadding = DatabaseListPadding,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(abilities, key = { it.id }) { ability ->
                    val tint = typeTint("grass")
                    TintedCard(tint, onClick = { sheet = ability.name }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                NameFormatter.pretty(ability.name),
                                style = MaterialTheme.typography.titleMedium,
                                color = tint.onContainer,
                                modifier = Modifier.weight(1f),
                            )
                            ability.generation?.let { TintPill("Gen ${NameFormatter.generationRoman(it)}", tint) }
                        }
                        (ability.description ?: ability.shortDescription)?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = tint.onContainer.copy(alpha = 0.9f))
                        }
                        val bearers = ability.pokemon.filter { it.speciesId != null && it.speciesId < 10000 }
                        if (bearers.isNotEmpty()) {
                            Text(
                                "${bearers.size} Pokémon have it",
                                style = MaterialTheme.typography.labelMedium,
                                color = tint.onContainer.copy(alpha = 0.75f),
                            )
                        }
                    }
                }
            }
        }
    }

    sheet?.let { name ->
        ReferenceSheet(
            target = ReferenceTarget.Ability(name),
            onDismiss = { sheet = null },
            onOpenPokemon = onPokemonClick,
        )
    }
}
