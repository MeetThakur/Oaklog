package com.oaklog.app.presentation.detail.tabs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oaklog.app.domain.logic.Learnset
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.LearnEntry
import com.oaklog.app.domain.model.MoveInfo
import com.oaklog.app.presentation.components.Card
import com.oaklog.app.presentation.components.DropdownOption
import com.oaklog.app.presentation.components.OakDropdown
import com.oaklog.app.presentation.components.TypeBadge

@Composable
fun MovesTab(
    learnset: List<LearnEntry>,
    moveDetails: Map<String, MoveInfo>,
    selectedVersionGroup: String?,
    selectedMethod: String,
    onSelectVersionGroup: (String) -> Unit,
    onSelectMethod: (String) -> Unit,
    onExpandMove: (String) -> Unit,
    onOpenMove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (learnset.isEmpty()) {
            Card {
                Text(
                    text = "No moves recorded for this Pokémon.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp),
                )
            }
            return@Column
        }

        val groups = remember(learnset) { Learnset.versionGroups(learnset) }
        val group = selectedVersionGroup ?: groups.first()
        val methods = remember(learnset, group) { Learnset.methods(learnset, group) }
        val method = selectedMethod.takeIf { it in methods } ?: methods.firstOrNull().orEmpty()
        val rows = remember(learnset, group, method) { Learnset.select(learnset, group, method) }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OakDropdown(
                label = "Game",
                options = groups.map { DropdownOption(it, NameFormatter.pretty(it)) },
                selected = group,
                onSelect = onSelectVersionGroup,
                modifier = Modifier.weight(1.4f),
            )
            OakDropdown(
                label = "Learned by",
                options = methods.map { DropdownOption(it, Learnset.prettyMethod(it)) },
                selected = method,
                onSelect = onSelectMethod,
                modifier = Modifier.weight(1f),
            )
        }

        Card {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "${Learnset.prettyMethod(method)} · ${rows.size}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                rows.forEachIndexed { index, entry ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    MoveRow(
                        entry = entry,
                        showLevel = method == "level-up",
                        info = moveDetails[entry.moveName],
                        onExpand = { onExpandMove(entry.moveName) },
                        onOpen = { onOpenMove(entry.moveName) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MoveRow(
    entry: LearnEntry,
    showLevel: Boolean,
    info: MoveInfo?,
    onExpand: () -> Unit,
    onOpen: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                expanded = !expanded
                if (expanded) onExpand()
            }
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showLevel) {
                Text(
                    text = if (entry.level == 0) "—" else "Lv ${entry.level}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(52.dp),
                )
            }
            Text(
                text = NameFormatter.pretty(entry.moveName),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        if (expanded) {
            Spacer(Modifier.height(10.dp))
            if (info == null) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                    trackColor = MaterialTheme.colorScheme.outline,
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    info.type?.let { TypeBadge(type = it, small = true) }
                    info.damageClass?.let {
                        Text(
                            text = NameFormatter.pretty(it),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    MoveStat("Power", info.power?.toString() ?: "—")
                    MoveStat("Accuracy", info.accuracy?.let { "$it%" } ?: "—")
                    MoveStat("PP", info.pp?.toString() ?: "—")
                }
                info.description?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                    )
                }
                TextButton(onClick = onOpen, contentPadding = PaddingValues(0.dp)) {
                    Text("Full entry and who learns it")
                }
            }
        }
    }
}

@Composable
private fun MoveStat(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
