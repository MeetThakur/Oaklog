package com.oaklog.app.presentation.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.CoverageResult
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.logic.TeamAnalysis
import com.oaklog.app.domain.model.Team
import com.oaklog.app.domain.model.TeamMember
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.ChipShape
import com.oaklog.app.presentation.theme.typeTint

/**
 * The squad at a glance: six slots as a tinted grid, then which of the 18 types the team can hit
 * super-effectively. Tapping a filled slot edits that member; an empty slot adds one.
 */
@Composable
fun SquadBoard(
    team: Team,
    analysis: TeamAnalysis?,
    coverage: CoverageResult?,
    onAddMember: () -> Unit,
    onSelectMember: (TeamMember) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (row in 0 until 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (col in 0 until 3) {
                    val member = team.members.getOrNull(row * 3 + col)
                    SquadSlot(
                        member = member,
                        onClick = { if (member != null) onSelectMember(member) else onAddMember() },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        if (analysis != null && team.members.isNotEmpty()) CoverageRow(analysis, coverage)
    }
}

@Composable
private fun SquadSlot(member: TeamMember?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = CardShape
    if (member == null) {
        val dash = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        Box(
            modifier = modifier
                .height(96.dp)
                .clip(shape)
                .drawBehind {
                    drawRoundRect(
                        color = dash,
                        cornerRadius = CornerRadius(24.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                    )
                }
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add member", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val tint = typeTint(member.types.firstOrNull())
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(shape)
            .background(tint.container)
            .clickable(onClick = onClick),
    ) {
        // The art disc bleeds off the corner like the Pokédex cards, so the board reads as the same family.
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 18.dp, y = 18.dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(tint.art),
        )
        AsyncImage(
            model = member.artworkUrl,
            contentDescription = member.pokemonName,
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(64.dp),
            contentScale = ContentScale.Fit,
        )
        Column(Modifier.align(Alignment.TopStart).padding(start = 10.dp, top = 8.dp, end = 8.dp)) {
            Text(
                text = member.nickname ?: NameFormatter.pretty(member.pokemonName),
                style = MaterialTheme.typography.labelLarge,
                color = tint.onContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = member.types.joinToString(" · ") { NameFormatter.pretty(it) },
                style = MaterialTheme.typography.labelSmall,
                color = tint.onContainer.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * All 18 types in one scrollable row. A type is colored when the team hits it super-effectively;
 * an outlined chip means it is only covered thanks to a chosen move, not a member's own types.
 */
@Composable
private fun CoverageRow(analysis: TeamAnalysis, coverage: CoverageResult?) {
    val all = analysis.defensive.map { it.attackType }
    // Until move data arrives (or if it can't load) fall back to own-type coverage.
    val covered = coverage?.covered ?: analysis.offensiveCoverage
    val viaMoves = coverage?.onlyViaMoves.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = buildString {
                append("Coverage  ${covered.size}/${all.size}")
                if (viaMoves.isNotEmpty()) append("  ·  ${viaMoves.size} from moves (outlined)")
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            all.forEach { type ->
                val isCovered = type in covered
                val tint = typeTint(type)
                val outlined = type in viaMoves
                Text(
                    text = NameFormatter.pretty(type),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCovered) tint.onContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clip(ChipShape)
                        .background(if (isCovered) tint.container else MaterialTheme.colorScheme.surfaceVariant)
                        .let { if (outlined) it.border(1.5.dp, tint.onContainer.copy(alpha = 0.55f), ChipShape) else it }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}
