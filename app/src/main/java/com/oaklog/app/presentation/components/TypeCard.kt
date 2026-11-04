package com.oaklog.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.CollectionStatus
import com.oaklog.app.domain.model.PokemonSummary
import com.oaklog.app.presentation.navigation.sharedArtwork
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.TypeTint
import com.oaklog.app.presentation.theme.typeTint

private fun artworkUrl(id: Int) =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

/**
 * A Pokémon painted in its own type color: number, name and type pills on the left, and an oversized
 * art disc bleeding off the bottom-right corner with the artwork overlapping it. The card clips both,
 * which is what makes the art look like it is stepping out of the frame.
 */
@Composable
fun TypeCard(
    pokemon: PokemonSummary,
    collectionEntry: CollectionEntry?,
    onClick: () -> Unit,
    onCycleStatus: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val tint = typeTint(pokemon.types.firstOrNull())
    val height = if (compact) 104.dp else 158.dp
    val disc = if (compact) 108.dp else 128.dp
    val art = if (compact) 86.dp else 102.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CardShape)
            .background(tint.container)
            .clickable(onClick = onClick),
    ) {
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = disc * 0.24f, y = disc * 0.24f)
                .size(disc)
                .clip(CircleShape)
                .background(tint.art),
        )
        AsyncImage(
            model = artworkUrl(pokemon.id),
            contentDescription = pokemon.name,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp, y = 12.dp)
                .size(art)
                .sharedArtwork(pokemon.id),
            contentScale = ContentScale.Fit,
        )

        // Number and the two toggles share the first row so a long name never runs under a badge.
        Column(Modifier.padding(start = 14.dp, top = 8.dp, end = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = NameFormatter.dexNumber(pokemon.id),
                    style = MaterialTheme.typography.labelMedium,
                    color = tint.onContainer.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f),
                )
                StatusBadge(
                    status = collectionEntry?.status ?: CollectionStatus.NOT_CAUGHT,
                    tint = tint,
                    onClick = onCycleStatus,
                )
                FavoriteIconButton(
                    isFavorite = collectionEntry?.favorite == true,
                    onToggle = onToggleFavorite,
                    tint = tint,
                )
            }
            Text(
                text = pokemon.name,
                style = MaterialTheme.typography.titleMedium,
                color = tint.onContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 6.dp),
            )
            Spacer(Modifier.height(6.dp))
            if (compact) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pokemon.types.forEach { TypeBadge(type = it, small = true, onTint = tint) }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    pokemon.types.forEach { TypeBadge(type = it, small = true, onTint = tint) }
                }
            }
        }
    }
}

/** Round badge for collection status on a tinted surface: filled when caught, outlined otherwise. */
@Composable
fun StatusBadge(
    status: CollectionStatus,
    tint: TypeTint,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val caught = status == CollectionStatus.CAUGHT
    val icon = when (status) {
        CollectionStatus.CAUGHT -> Icons.Filled.Check
        CollectionStatus.SEEN -> Icons.Filled.Visibility
        CollectionStatus.NOT_CAUGHT -> Icons.Filled.Add
    }
    val description = when (status) {
        CollectionStatus.CAUGHT -> "Caught"
        CollectionStatus.SEEN -> "Seen"
        CollectionStatus.NOT_CAUGHT -> "Not caught"
    }
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(if (caught) tint.onContainer else tint.chip)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (caught) tint.container else tint.onContainer,
            modifier = Modifier.size(16.dp),
        )
    }
}
