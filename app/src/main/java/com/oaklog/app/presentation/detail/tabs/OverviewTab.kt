package com.oaklog.app.presentation.detail.tabs

import com.oaklog.app.presentation.components.Card
import com.oaklog.app.presentation.components.FilterChip
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.oaklog.app.domain.model.HeldItem
import com.oaklog.app.domain.logic.GrowthRates
import com.oaklog.app.domain.logic.HeldItems
import com.oaklog.app.domain.logic.Matchups
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.AbilityInfo
import com.oaklog.app.domain.model.PokemonDetail
import com.oaklog.app.presentation.components.DropdownOption
import com.oaklog.app.presentation.components.OakDropdown
import com.oaklog.app.presentation.components.TypeBadge

@Composable
fun OverviewTab(
    detail: PokemonDetail,
    selectedDexVersion: String?,
    onSelectDexVersion: (String) -> Unit,
    abilityInfo: Map<String, AbilityInfo> = emptyMap(),
    onAbilityClick: (String) -> Unit = {},
    matchups: Matchups? = null,
    onItemClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Pokédex Description with Game Version Switcher
        if (detail.dexEntries.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Professor Oak's Field Notes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))

                    val activeText = detail.dexEntries.firstOrNull { it.versionName == selectedDexVersion }
                        ?: detail.dexEntries.first()
                    Text(
                        text = activeText.text,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(Modifier.height(12.dp))

                    OakDropdown(
                        label = "Game version",
                        options = detail.dexEntries.map { DropdownOption(it.versionName, NameFormatter.pretty(it.versionName)) },
                        selected = selectedDexVersion,
                        onSelect = onSelectDexVersion,
                    )
                }
            }
        }

        // Physical Dimensions (Height, Weight, Base Exp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                DimensionItem(
                    label = "Height",
                    value = NameFormatter.heightMeters(detail.heightDm),
                )
                DimensionItem(
                    label = "Weight",
                    value = NameFormatter.weightKilograms(detail.weightHg),
                )
                detail.baseExperience?.let {
                    DimensionItem(
                        label = "Base Exp",
                        value = it.toString(),
                    )
                }
            }
        }

        // Abilities
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Abilities",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))

                detail.abilities.forEachIndexed { index, ability ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onAbilityClick(ability.name) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = NameFormatter.pretty(ability.name),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (ability.isHidden) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Text(
                                    text = "Hidden",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    val definition = abilityInfo[ability.name]?.let { it.shortDescription ?: it.description }
                    if (definition != null) {
                        Text(
                            text = definition,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                            modifier = Modifier.clickable { onAbilityClick(ability.name) },
                        )
                    } else {
                        Text(
                            text = "Loading definition…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }

        if (matchups != null && !matchups.isEmpty) MatchupsCard(matchups)

        if (detail.heldItems.isNotEmpty()) {
            HeldItemsCard(detail.heldItems, selectedDexVersion, onItemClick)
        }

        // Breeding & Capture Info
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Breeding & Capture",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Gender Ratio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Gender Ratio",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (detail.femaleEighths == null) {
                        Text(
                            text = "Genderless",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    } else {
                        val femalePct = (detail.femaleEighths * 100.0 / 8.0).toInt()
                        val malePct = 100 - femalePct
                        Text(
                            text = "♂ $malePct%  •  ♀ $femalePct%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // Egg Groups
                if (detail.eggGroups.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Egg Groups",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = detail.eggGroups.joinToString(", ") { NameFormatter.pretty(it) },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // Growth Rate
                detail.growthRate?.let { slug ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Growth Rate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = GrowthRates.curve(slug)?.label ?: NameFormatter.pretty(slug),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            GrowthRates.curve(slug)?.let {
                                Text(
                                    text = "${"%,d".format(it.expToLevel100)} EXP to Lv 100",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Capture Rate
                detail.captureRate?.let { rate ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Capture Rate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "$rate / 255",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // Generation
                detail.generation?.let { gen ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Introduced In",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Generation ${NameFormatter.generationRoman(gen)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DimensionItem(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}


/**
 * Weak against, resists, immune and strong against, each as type pills with the multiplier beside
 * them. Typing only: an ability such as Levitate can change the real result, which the note says.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchupsCard(m: Matchups) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Type matchups",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            MatchupGroup("Weak against", m.weak4.map { it to "×4" } + m.weak2.map { it to "×2" })
            MatchupGroup("Resists", m.resist2.map { it to "×½" } + m.resist4.map { it to "×¼" })
            MatchupGroup("Immune to", m.immune.map { it to "×0" })
            MatchupGroup("Strong against", m.strongAgainst.map { it to "×2" })
            Text(
                text = "Based on type only. Abilities and moves can change the real result.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchupGroup(label: String, entries: List<Pair<String, String>>) {
    if (entries.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            entries.forEach { (type, multiplier) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TypeBadge(type = type, small = true)
                    Text(
                        text = multiplier,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Items wild specimens may hold, with the chance in the game picked in the Pokédex-entry dropdown
 * above. Tapping an item opens its entry, which lists who else holds it.
 */
@Composable
private fun HeldItemsCard(items: List<HeldItem>, game: String?, onItemClick: (String) -> Unit) {
    // If the picked game recorded nothing, say so rather than showing another game's numbers.
    val shown = HeldItems.forGame(items, game)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Held items",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (game == null) "Wild ones may hold these." else "Wild ones in ${NameFormatter.pretty(game)} may hold these.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (shown.isEmpty()) {
                Text(
                    text = "Nothing recorded for this game.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            shown.forEach { held ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onItemClick(held.item) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AsyncImage(
                        model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/${held.item}.png",
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Text(
                        text = NameFormatter.pretty(held.item),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    HeldItems.rarityLabel(held.entries, game)?.let {
                        Text(it, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
