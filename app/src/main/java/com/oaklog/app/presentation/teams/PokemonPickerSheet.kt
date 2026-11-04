package com.oaklog.app.presentation.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.PokemonSummary
import com.oaklog.app.presentation.components.Card
import com.oaklog.app.presentation.components.TypeBadge
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.ChipShape
import com.oaklog.app.presentation.theme.typeTint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonPickerSheet(
    pokemonList: List<PokemonSummary>,
    onSelectPokemon: (PokemonSummary) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Add Squad Member",
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(pokemonList, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) pokemonList
        else {
            val num = q.toIntOrNull()
            pokemonList.filter {
                (num != null && it.id == num) || it.name.lowercase().contains(q)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or number...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = ChipShape,
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(filteredList, key = { it.id }) { pokemon ->
                    val artworkUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${pokemon.id}.png"
                    val tint = typeTint(pokemon.types.firstOrNull())

                    Card(
                        onClick = {
                            onSelectPokemon(pokemon)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = tint.container),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(tint.art),
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model = artworkUrl,
                                    contentDescription = pokemon.name,
                                    modifier = Modifier.size(42.dp),
                                    contentScale = ContentScale.Fit,
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = NameFormatter.dexNumber(pokemon.id),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tint.onContainer.copy(alpha = 0.7f),
                                )
                                Text(
                                    text = pokemon.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = tint.onContainer,
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                pokemon.types.forEach { type ->
                                    TypeBadge(type = type, small = true, onTint = tint)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
