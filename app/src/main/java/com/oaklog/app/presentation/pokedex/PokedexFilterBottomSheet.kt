package com.oaklog.app.presentation.pokedex

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oaklog.app.domain.logic.CaughtFilter
import com.oaklog.app.domain.logic.PokedexFilters
import com.oaklog.app.domain.logic.SortField
import com.oaklog.app.presentation.components.TypeBadge

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PokedexFilterBottomSheet(
    filters: PokedexFilters,
    onApply: (PokedexFilters) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    var state by remember { mutableStateOf(filters) }

    val allTypes = listOf(
        "normal", "fire", "water", "grass", "electric", "ice",
        "fighting", "poison", "ground", "flying", "psychic", "bug",
        "rock", "ghost", "dragon", "steel", "fairy", "dark"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Filter & Sort",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(onClick = {
                    onReset()
                    onDismiss()
                }) {
                    Text("Reset All")
                }
            }

            // Sort Section
            Text("Sort By", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SortField.entries.forEach { field ->
                    FilterChip(
                        selected = state.sortField == field,
                        onClick = { state = state.copy(sortField = field) },
                        label = {
                            Text(when (field) {
                                SortField.NUMBER -> "Number"
                                SortField.NAME -> "Name"
                                SortField.HEIGHT -> "Height"
                                SortField.WEIGHT -> "Weight"
                                SortField.STAT_TOTAL -> "Base Stat Total"
                            })
                        }
                    )
                }
            }

            // Sort Direction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = !state.sortDescending,
                    onClick = { state = state.copy(sortDescending = false) },
                    label = { Text("Ascending") },
                )
                FilterChip(
                    selected = state.sortDescending,
                    onClick = { state = state.copy(sortDescending = true) },
                    label = { Text("Descending") },
                )
            }

            HorizontalDivider()

            // Generation Section
            Text("Generation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                (1..9).forEach { gen ->
                    val isSelected = state.generations.contains(gen)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updated = if (isSelected) state.generations - gen else state.generations + gen
                            state = state.copy(generations = updated)
                        },
                        label = { Text("Gen $gen") },
                    )
                }
            }

            HorizontalDivider()

            // Type Section
            Text("Types", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                allTypes.forEach { type ->
                    val isSelected = state.types.contains(type)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updated = if (isSelected) state.types - type else state.types + type
                            state = state.copy(types = updated)
                        },
                        label = {
                            Text(type.replaceFirstChar { it.uppercase() })
                        },
                    )
                }
            }

            HorizontalDivider()

            // Status Section
            Text("Collection Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CaughtFilter.entries.forEach { cf ->
                    FilterChip(
                        selected = state.caught == cf,
                        onClick = { state = state.copy(caught = cf) },
                        label = {
                            Text(when (cf) {
                                CaughtFilter.ANY -> "All"
                                CaughtFilter.CAUGHT -> "Caught"
                                CaughtFilter.UNCAUGHT -> "Uncaught"
                            })
                        }
                    )
                }
            }

            // Toggles (Legendary, Mythical, Favorites)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Legendary Pokémon Only", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = state.legendaryOnly,
                    onCheckedChange = { state = state.copy(legendaryOnly = it) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Mythical Pokémon Only", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = state.mythicalOnly,
                    onCheckedChange = { state = state.copy(mythicalOnly = it) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Favorites Only", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = state.favoritesOnly,
                    onCheckedChange = { state = state.copy(favoritesOnly = it) },
                )
            }

            Spacer(Modifier.height(8.dp))

            // Apply Button
            Button(
                onClick = {
                    onApply(state)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text("Apply Filters (${state.activeCount} active)")
            }
        }
    }
}
