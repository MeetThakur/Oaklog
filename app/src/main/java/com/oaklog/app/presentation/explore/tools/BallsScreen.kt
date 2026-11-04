package com.oaklog.app.presentation.explore.tools

import androidx.compose.material.icons.filled.CatchingPokemon
import com.oaklog.app.presentation.components.DatabasePage
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.BallInfo
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.ItemInfo
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.ErrorView
import com.oaklog.app.presentation.components.FilterChip
import com.oaklog.app.presentation.components.LoadingView
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.ChipShape
import com.oaklog.app.presentation.theme.typeTint

/** Every Poké Ball with its catch multiplier and what it does, grouped by kind. */
@Composable
fun BallsScreen(
    viewModel: BallsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val categories = remember(state.balls) { state.balls.mapNotNull { it.category }.distinct() }
    val shown = remember(state.balls, state.category, state.query) {
        val q = state.query.trim().lowercase().replace('-', ' ')
        state.balls.filter { b ->
            (state.category == null || b.category == state.category) &&
                (q.isEmpty() || b.name.replace('-', ' ').contains(q))
        }
    }

    DatabasePage(
        title = "Poké Balls",
        subtitle = "Catch multipliers and effects",
        color = "poison",
        icon = Icons.Default.CatchingPokemon,
        onNavigateBack = onNavigateBack,
        compact = true,
        modifier = modifier,
    ) {
        when {
            state.isLoading -> LoadingView(message = "Loading Poké Balls...", modifier = Modifier)
            state.failed -> ErrorView(
                message = "Couldn't load the Poké Balls. Check your connection and try again.",
                onRetry = viewModel::load,
                modifier = Modifier,
            )
            else -> Column(Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::setQuery,
                    placeholder = { Text("Search ${state.balls.size} balls") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    },
                    singleLine = true,
                    shape = ChipShape,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(selected = state.category == null, onClick = { viewModel.setCategory(null) }, label = { Text("All") })
                    categories.forEach { c ->
                        FilterChip(
                            selected = state.category == c,
                            onClick = { viewModel.setCategory(if (state.category == c) null else c) },
                            label = { Text(BallInfo.categoryLabel(c)) },
                        )
                    }
                }
                if (shown.isEmpty()) {
                    EmptyView(
                        title = "No ball matches",
                        subtitle = "Try another kind, or clear the search.",
                        icon = Icons.Outlined.SearchOff,
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(shown, key = { it.id }) { BallCard(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BallCard(ball: ItemInfo) {
    val multiplier = BallInfo.multiplier(ball.effect)
    val detail = BallInfo.detail(ball.effect)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = ball.spriteUrl,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(NameFormatter.pretty(ball.name), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f, fill = false))
                if (multiplier != null) {
                    val tint = typeTint("electric")
                    Text(
                        multiplier,
                        style = MaterialTheme.typography.labelMedium,
                        color = tint.onContainer,
                        modifier = Modifier.clip(ChipShape).background(tint.container).padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
            Text(
                BallInfo.categoryLabel(ball.category),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            detail?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            ball.description?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
