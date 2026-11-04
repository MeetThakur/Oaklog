package com.oaklog.app.presentation.detail.tabs

import com.oaklog.app.presentation.components.Card
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oaklog.app.domain.model.PokemonDetail
import com.oaklog.app.presentation.components.StatBar

@Composable
fun StatsTab(
    detail: PokemonDetail,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Base Stats Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Base Stats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Max 255",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Bars take the Pokémon's own type color, darkened a touch so pale types still read.
                val barColor = androidx.compose.ui.graphics.lerp(
                    com.oaklog.app.presentation.theme.PokemonTypeColors.color(detail.types.firstOrNull() ?: "unknown"),
                    androidx.compose.ui.graphics.Color.Black,
                    0.2f,
                )
                detail.stats.forEach { stat ->
                    StatBar(
                        label = stat.label,
                        value = stat.value,
                        barColor = barColor,
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Total BST
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Total Base Stat (BST)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = detail.baseStatTotal.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // Stat Ranges Explanation
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Stat Evaluation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))

                val tier = when {
                    detail.baseStatTotal >= 670 -> "Legendary Tier (Exceptional combat capabilities)"
                    detail.baseStatTotal >= 600 -> "Pseudo-Legendary / High Tier"
                    detail.baseStatTotal >= 500 -> "Fully Evolved / Tournament Tier"
                    detail.baseStatTotal >= 400 -> "Mid Tier / Intermediate Stage"
                    else -> "Base Form / Developing Stage"
                }

                Text(
                    text = tier,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Stat values directly influence in-battle speed, resilience, and damage output. Stat gauges reflect base natural limits across all known species.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

