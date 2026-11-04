package com.oaklog.app.presentation.detail.tabs

import com.oaklog.app.presentation.components.Card
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.EvolutionNode

@Composable
fun EvolutionTab(
    evolutionRoot: EvolutionNode?,
    currentSpeciesId: Int,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Evolution Chain",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start),
                )
                Spacer(Modifier.height(16.dp))

                if (evolutionRoot == null) {
                    Text(
                        text = "This Pokémon does not evolve.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (evolutionRoot.children.isEmpty()) {
                    EvolutionPokemonNode(
                        node = evolutionRoot,
                        isCurrent = evolutionRoot.speciesId == currentSpeciesId,
                        onClick = { onPokemonClick(evolutionRoot.speciesId) },
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "This species does not have known evolutions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    EvolutionChainView(
                        node = evolutionRoot,
                        currentSpeciesId = currentSpeciesId,
                        onPokemonClick = onPokemonClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun EvolutionChainView(
    node: EvolutionNode,
    currentSpeciesId: Int,
    onPokemonClick: (Int) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EvolutionPokemonNode(
            node = node,
            isCurrent = node.speciesId == currentSpeciesId,
            onClick = { onPokemonClick(node.speciesId) },
        )

        node.children.forEach { child ->
            EvolutionBranchItem(
                childNode = child,
                currentSpeciesId = currentSpeciesId,
                onPokemonClick = onPokemonClick,
            )
        }
    }
}

@Composable
private fun EvolutionBranchItem(
    childNode: EvolutionNode,
    currentSpeciesId: Int,
    onPokemonClick: (Int) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Requirement indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            val reqText = childNode.requirements.firstOrNull() ?: "Evolves"
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = reqText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }

        // Child Pokémon
        EvolutionPokemonNode(
            node = childNode,
            isCurrent = childNode.speciesId == currentSpeciesId,
            onClick = { onPokemonClick(childNode.speciesId) },
        )

        // Grandchildren (e.g. 3-stage evolution)
        childNode.children.forEach { grandChild ->
            EvolutionBranchItem(
                childNode = grandChild,
                currentSpeciesId = currentSpeciesId,
                onPokemonClick = onPokemonClick,
            )
        }
    }
}

@Composable
private fun EvolutionPokemonNode(
    node: EvolutionNode,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    val artworkUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${node.speciesId}.png"

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .widthIn(min = 160.dp),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AsyncImage(
                model = artworkUrl,
                contentDescription = node.name,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit,
            )
            Column {
                Text(
                    text = NameFormatter.dexNumber(node.speciesId),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = NameFormatter.pretty(node.name),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                if (node.isBaby) {
                    Text(
                        text = "Baby Pokémon",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

