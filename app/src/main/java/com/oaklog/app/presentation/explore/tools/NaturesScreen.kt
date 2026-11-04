package com.oaklog.app.presentation.explore.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.oaklog.app.domain.logic.Nature
import com.oaklog.app.domain.logic.Natures
import com.oaklog.app.presentation.components.DropdownOption
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.OakDropdown
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.ChipShape
import com.oaklog.app.presentation.theme.typeTint

private val STAT_NAMES = linkedMapOf(
    "attack" to "Attack",
    "defense" to "Defense",
    "special-attack" to "Sp. Atk",
    "special-defense" to "Sp. Def",
    "speed" to "Speed",
)

/** Every nature, with what it raises and lowers and the flavors it likes; filter by either stat. */
@Composable
fun NaturesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var up by remember { mutableStateOf<String?>(null) }
    var down by remember { mutableStateOf<String?>(null) }
    val shown = remember(up, down) { Natures.matching(up, down) }
    val statOptions = listOf(DropdownOption("", "Any")) + STAT_NAMES.map { DropdownOption(it.key, it.value) }
    val (neutral, changing) = shown.partition { it.isNeutral }

    Scaffold(
        topBar = { OakTopBar(title = "Natures", canNavigateBack = true, onNavigateBack = onNavigateBack) },
        modifier = modifier,
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    "A nature raises one stat by 10% and lowers another by 10%. HP is never affected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OakDropdown(
                        label = "Raises",
                        options = statOptions,
                        selected = up ?: "",
                        onSelect = { up = it.ifEmpty { null } },
                        modifier = Modifier.weight(1f),
                    )
                    OakDropdown(
                        label = "Lowers",
                        options = statOptions,
                        selected = down ?: "",
                        onSelect = { down = it.ifEmpty { null } },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (shown.isEmpty()) {
                item {
                    // Raising and lowering the same stat is not a nature.
                    EmptyView(
                        title = "No such nature",
                        subtitle = "No nature raises and lowers the same stat. Pick two different ones.",
                    )
                }
            }
            items(changing, key = { it.name }) { NatureCard(it) }

            if (neutral.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CardShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Neutral", style = MaterialTheme.typography.titleMedium)
                        Text(
                            neutral.joinToString(", ") { it.name },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "No stat changes, and no flavor preferences.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NatureCard(nature: Nature) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(nature.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "Likes ${nature.likes?.lowercase()} · dislikes ${nature.dislikes?.lowercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StatDelta("+", STAT_NAMES.getValue(nature.up!!), "grass")
            StatDelta("−", STAT_NAMES.getValue(nature.down!!), "fire")
        }
    }
}

/** Raised stats use the green tint and lowered ones the red tint, so direction reads at a glance. */
@Composable
private fun StatDelta(sign: String, stat: String, color: String) {
    val tint = typeTint(color)
    Text(
        text = "$sign $stat",
        style = MaterialTheme.typography.labelMedium,
        color = tint.onContainer,
        modifier = Modifier
            .clip(ChipShape)
            .background(tint.container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
