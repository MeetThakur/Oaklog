package com.oaklog.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.PokemonSummary
import com.oaklog.app.presentation.theme.OakOutlineDark
import com.oaklog.app.presentation.theme.OakOutlineLight
import com.oaklog.app.presentation.theme.PokemonTypeColors

@Composable
fun PokemonGridCard(
    pokemon: PokemonSummary,
    collectionEntry: CollectionEntry?,
    onClick: () -> Unit,
    onCycleStatus: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val typeBg = PokemonTypeColors.cardBackground(pokemon.types, isDark)
    val artworkUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${pokemon.id}.png"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(typeBg)
                .padding(10.dp),
        ) {
            // Header: # number and favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = NameFormatter.dexNumber(pokemon.id),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FavoriteIconButton(
                    isFavorite = collectionEntry?.favorite == true,
                    onToggle = onToggleFavorite,
                )
            }

            // Pokémon Official Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = artworkUrl,
                    contentDescription = pokemon.name,
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(Modifier.height(6.dp))

            // Pokémon Name
            Text(
                text = pokemon.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(4.dp))

            // Types
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                pokemon.types.forEach { type ->
                    TypeBadge(type = type, small = true)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Caught / Seen status
            CollectionStatusButton(
                entry = collectionEntry,
                onCycleStatus = onCycleStatus,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun PokemonListCard(
    pokemon: PokemonSummary,
    collectionEntry: CollectionEntry?,
    onClick: () -> Unit,
    onCycleStatus: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val typeBg = PokemonTypeColors.cardBackground(pokemon.types, isDark)
    val artworkUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${pokemon.id}.png"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(typeBg)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Artwork
            AsyncImage(
                model = artworkUrl,
                contentDescription = pokemon.name,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit,
            )

            Spacer(Modifier.width(12.dp))

            // Info column
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = NameFormatter.dexNumber(pokemon.id),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    pokemon.types.forEach { type ->
                        TypeBadge(type = type, small = true)
                    }
                }
            }

            // Action buttons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FavoriteIconButton(
                    isFavorite = collectionEntry?.favorite == true,
                    onToggle = onToggleFavorite,
                )
                CollectionStatusButton(
                    entry = collectionEntry,
                    onCycleStatus = onCycleStatus,
                )
            }
        }
    }
}
