package com.oaklog.app.presentation.explore.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.MoveInfo
import com.oaklog.app.presentation.components.DatabaseListPadding
import com.oaklog.app.presentation.components.DatabasePage
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.LoadingView
import com.oaklog.app.presentation.components.StatColumn
import com.oaklog.app.presentation.components.TintPill
import com.oaklog.app.presentation.components.TintedCard
import com.oaklog.app.presentation.theme.typeTint

@Composable
fun MovesDatabaseScreen(
    moves: List<MoveInfo>,
    isLoading: Boolean,
    onSearch: (String) -> Unit,
    onInit: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { onInit() }

    DatabasePage(
        title = "Moves",
        subtitle = if (moves.isEmpty()) "Power, accuracy, PP and effects" else "${moves.size} shown · search any move by name",
        color = "fire",
        icon = Icons.Default.AutoAwesome,
        onNavigateBack = onNavigateBack,
        query = query,
        onQueryChange = { query = it; onSearch(it) },
        searchHint = "Search moves",
        modifier = modifier,
    ) {
        when {
            isLoading && moves.isEmpty() -> LoadingView(message = "Searching moves...")
            moves.isEmpty() -> EmptyView(
                title = "No move found",
                subtitle = "Type a full move name, like flamethrower or quick attack.",
                icon = Icons.Outlined.SearchOff,
            )
            else -> LazyColumn(
                contentPadding = DatabaseListPadding,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(moves, key = { it.id }) { MoveCard(it) }
            }
        }
    }
}

/** Painted in the move's own type, so a glance tells Fire from Water before reading a word. */
@Composable
private fun MoveCard(move: MoveInfo) {
    val tint = typeTint(move.type)
    TintedCard(tint) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                NameFormatter.pretty(move.name),
                style = MaterialTheme.typography.titleMedium,
                color = tint.onContainer,
                modifier = Modifier.weight(1f),
            )
            move.type?.let { TintPill(NameFormatter.pretty(it), tint) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            StatColumn("Category", move.damageClass?.let { NameFormatter.pretty(it) } ?: "—", tint)
            StatColumn("Power", move.power?.toString() ?: "—", tint)
            StatColumn("Accuracy", move.accuracy?.let { "$it%" } ?: "—", tint)
            StatColumn("PP", move.pp?.toString() ?: "—", tint)
        }
        move.description?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = tint.onContainer.copy(alpha = 0.9f))
        }
    }
}
