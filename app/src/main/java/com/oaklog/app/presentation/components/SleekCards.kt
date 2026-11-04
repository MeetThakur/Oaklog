package com.oaklog.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.model.CollectionEntry
import com.oaklog.app.domain.model.CollectionStatus
import com.oaklog.app.domain.model.PokemonSummary
import com.oaklog.app.presentation.navigation.sharedArtwork
import com.oaklog.app.presentation.theme.PokemonTypeColors
import com.oaklog.app.presentation.theme.typeTint

private fun art(id: Int) =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

/** Dark mode gets round art medallions with a hairline; light mode gets soft rounded tiles. */
@Composable
private fun artShape(): Shape =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) CircleShape else RoundedCornerShape(20.dp)

/** "001", with the type shown as a colored dot and plain text: the only color on the row. */
@Composable
private fun TypeLine(types: List<String>) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        types.forEach { type ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(PokemonTypeColors.color(type)))
                Spacer(Modifier.width(5.dp))
                Text(
                    text = type.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusMark(entry: CollectionEntry?, onClick: () -> Unit) {
    val status = entry?.status ?: CollectionStatus.NOT_CAUGHT
    val caught = status == CollectionStatus.CAUGHT
    val ink = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (caught) ink else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = when (status) {
                CollectionStatus.CAUGHT -> Icons.Filled.Check
                CollectionStatus.SEEN -> Icons.Outlined.Visibility
                CollectionStatus.NOT_CAUGHT -> Icons.Filled.Add
            },
            contentDescription = when (status) {
                CollectionStatus.CAUGHT -> "Caught"
                CollectionStatus.SEEN -> "Seen"
                CollectionStatus.NOT_CAUGHT -> "Not caught"
            },
            tint = if (caught) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp),
        )
    }
}

/** List row: art, name over a type line, dex number, status and favorite. A hairline separates rows. */
@Composable
fun SleekRow(
    pokemon: PokemonSummary,
    collectionEntry: CollectionEntry?,
    onClick: () -> Unit,
    onCycleStatus: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = artShape()
    val tint = typeTint(pokemon.types.firstOrNull())
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(shape)
                    .background(tint.container)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline, shape),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = art(pokemon.id),
                    contentDescription = pokemon.name,
                    modifier = Modifier.size(48.dp).sharedArtwork(pokemon.id),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TypeLine(pokemon.types)
            }
            Text(
                text = "%03d".format(pokemon.id),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
            StatusMark(collectionEntry, onCycleStatus)
            FavoriteIconButton(isFavorite = collectionEntry?.favorite == true, onToggle = onToggleFavorite)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
    }
}

/** Grid tile: a hairline-bordered surface, the art on a quiet field, name and number below. */
@Composable
fun SleekTile(
    pokemon: PokemonSummary,
    collectionEntry: CollectionEntry?,
    onClick: () -> Unit,
    onCycleStatus: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    val tint = typeTint(pokemon.types.firstOrNull())
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.container),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = art(pokemon.id),
                contentDescription = pokemon.name,
                modifier = Modifier.size(104.dp).sharedArtwork(pokemon.id),
                contentScale = ContentScale.Fit,
            )
            Box(Modifier.align(Alignment.TopEnd).padding(6.dp)) { StatusMark(collectionEntry, onCycleStatus) }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = pokemon.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "%03d".format(pokemon.id),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { TypeLine(pokemon.types) }
            FavoriteIconButton(isFavorite = collectionEntry?.favorite == true, onToggle = onToggleFavorite)
        }
    }
}

/** Small overline over a large title, with the page's actions on the right. */
@Composable
fun SleekHeader(
    overline: String,
    title: String,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = overline,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        actions()
    }
}
