package com.oaklog.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.PokemonSummary
import com.oaklog.app.presentation.navigation.sharedArtwork
import com.oaklog.app.presentation.theme.PokemonTypeColors

private fun artworkUrl(id: Int) =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

/** Soft type-tinted disc behind the artwork: the only place type color fills a surface. */
@Composable
private fun ArtworkDisc(
    pokemon: PokemonSummary,
    size: androidx.compose.ui.unit.Dp,
    artSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val tint = PokemonTypeColors.color(pokemon.types.firstOrNull() ?: "unknown")
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(tint.copy(alpha = 0.22f), tint.copy(alpha = 0.04f)))),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = artworkUrl(pokemon.id),
            contentDescription = pokemon.name,
            modifier = Modifier.size(artSize).sharedArtwork(pokemon.id),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
fun PokemonGridCard(
    pokemon: PokemonSummary,
    collectionEntry: CollectionEntry?,
    onClick: () -> Unit,
    onCycleStatus: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = NameFormatter.dexNumber(pokemon.id),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(entry = collectionEntry, onCycleStatus = onCycleStatus)
                    FavoriteIconButton(
                        isFavorite = collectionEntry?.favorite == true,
                        onToggle = onToggleFavorite,
                    )
                }
            }

            ArtworkDisc(
                pokemon = pokemon,
                size = 112.dp,
                artSize = 96.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = pokemon.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                pokemon.types.forEach { TypeBadge(type = it, small = true) }
            }
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
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkDisc(pokemon = pokemon, size = 60.dp, artSize = 52.dp)

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = NameFormatter.dexNumber(pokemon.id),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pokemon.types.forEach { TypeBadge(type = it, small = true) }
                }
            }

            StatusDot(entry = collectionEntry, onCycleStatus = onCycleStatus)
            FavoriteIconButton(
                isFavorite = collectionEntry?.favorite == true,
                onToggle = onToggleFavorite,
            )
        }
    }
}
