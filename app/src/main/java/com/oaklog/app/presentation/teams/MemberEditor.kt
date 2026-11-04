package com.oaklog.app.presentation.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
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
import com.oaklog.app.domain.logic.Learnset
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.MoveInfo
import com.oaklog.app.domain.model.TeamMember
import com.oaklog.app.presentation.components.DropdownOption
import com.oaklog.app.presentation.components.OakDropdown
import com.oaklog.app.presentation.components.TypeBadge
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.ChipShape
import com.oaklog.app.presentation.theme.typeTint

private const val SLOT_COUNT = 4

/**
 * Edit one team member. Ability and moves are picked, never typed: the ability must be one the
 * Pokémon can really have, and each of the four move slots opens a list of moves it can really learn.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberEditorSheet(
    member: TeamMember,
    options: MemberOptions?,
    optionsFailed: Boolean,
    loadMove: suspend (String) -> MoveInfo?,
    onSave: (TeamMember) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    var nickname by remember { mutableStateOf(member.nickname.orEmpty()) }
    var ability by remember { mutableStateOf(member.ability?.let(Learnset::slug)) }
    val slots = remember {
        mutableStateListOf<String?>().apply {
            val saved = member.moves.map(Learnset::slug)
            repeat(SLOT_COUNT) { add(saved.getOrNull(it)) }
        }
    }
    var pickingSlot by remember { mutableStateOf<Int?>(null) }
    var confirmRemove by remember { mutableStateOf(false) }
    val tint = typeTint(member.types.firstOrNull())
    val legalNames = remember(options) { options?.learnset?.map { it.moveName }?.toSet() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Header: the member in its own color.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(tint.container)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(tint.art),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = member.artworkUrl,
                        contentDescription = member.pokemonName,
                        modifier = Modifier.size(68.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Column {
                    Text(
                        NameFormatter.pretty(member.pokemonName),
                        style = MaterialTheme.typography.titleLarge,
                        color = tint.onContainer,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        member.types.forEach { TypeBadge(type = it, small = true, onTint = tint) }
                    }
                }
            }

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it.take(24) },
                label = { Text("Nickname (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                ),
            )

            // ---- Ability ----------------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Ability")
                when {
                    options == null && optionsFailed -> Hint("Couldn't load this Pokémon's abilities. Reopen to retry.")
                    options == null -> Hint("Loading abilities…")
                    else -> {
                        val legal = options.abilities.map { it.name }
                        // A value typed before this picker existed may not be legal; say so, don't hide it.
                        if (ability != null && ability !in legal) {
                            AbilityRow(
                                title = NameFormatter.pretty(ability!!),
                                subtitle = "Not an ability this Pokémon can have. Pick one below.",
                                hidden = false,
                                selected = true,
                                warning = true,
                                onClick = {},
                            )
                        }
                        options.abilities.forEach { ref ->
                            val info = options.abilityInfo[ref.name]
                            AbilityRow(
                                title = NameFormatter.pretty(ref.name),
                                subtitle = info?.shortDescription ?: info?.description,
                                hidden = ref.isHidden,
                                selected = ability == ref.name,
                                warning = false,
                                onClick = { ability = ref.name },
                            )
                        }
                    }
                }
            }

            // ---- Moves ------------------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Moves  ${slots.count { it != null }}/$SLOT_COUNT")
                for (row in 0 until SLOT_COUNT / 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (col in 0 until 2) {
                            val index = row * 2 + col
                            MoveSlot(
                                name = slots[index],
                                notLearnable = legalNames != null && slots[index] != null && slots[index] !in legalNames,
                                loadMove = loadMove,
                                onClick = { pickingSlot = index },
                                onClear = { slots[index] = null },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                if (options != null && options.learnset.isEmpty()) {
                    Hint("Couldn't load this Pokémon's move list, so moves can't be chosen right now.")
                }
            }

            Button(
                onClick = {
                    onSave(
                        member.copy(
                            nickname = nickname.trim().ifEmpty { null },
                            ability = ability,
                            moves = slots.filterNotNull(),
                        ),
                    )
                },
                shape = ChipShape,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Save", style = MaterialTheme.typography.labelLarge) }

            TextButton(
                onClick = { confirmRemove = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text("Remove from team") }
        }
    }

    pickingSlot?.let { slot ->
        if (options != null) {
            MovePickerSheet(
                options = options,
                chosen = slots.filterIndexed { i, _ -> i != slot }.filterNotNull().toSet(),
                loadMove = loadMove,
                onPick = { slots[slot] = it },
                onDismiss = { pickingSlot = null },
            )
        } else {
            pickingSlot = null
        }
    }

    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Remove ${NameFormatter.pretty(member.pokemonName)}?") },
            text = { Text("They'll leave this team. Their moves and ability are lost.") },
            confirmButton = {
                TextButton(
                    onClick = { confirmRemove = false; onRemove() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text("Keep") } },
        )
    }
}

@Composable
private fun SectionLabel(text: String) = Text(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.onSurface,
)

@Composable
private fun Hint(text: String) = Text(
    text = text,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
private fun AbilityRow(
    title: String,
    subtitle: String?,
    hidden: Boolean,
    selected: Boolean,
    warning: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val on = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(top = 2.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = on)
                if (hidden) {
                    Text(
                        "Hidden",
                        style = MaterialTheme.typography.labelSmall,
                        color = on.copy(alpha = 0.8f),
                        modifier = Modifier
                            .clip(ChipShape)
                            .background(on.copy(alpha = 0.14f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (warning) MaterialTheme.colorScheme.error else on.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MoveSlot(
    name: String?,
    notLearnable: Boolean,
    loadMove: suspend (String) -> MoveInfo?,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val info by produceState<MoveInfo?>(null, name) { value = name?.let { loadMove(it) } }
    val tint = typeTint(info?.type)
    val dash = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)

    Box(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(20.dp))
            .let {
                if (name == null) {
                    it.drawBehind {
                        drawRoundRect(
                            color = dash,
                            cornerRadius = CornerRadius(20.dp.toPx()),
                            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                        )
                    }
                } else it.background(if (info != null) tint.container else MaterialTheme.colorScheme.surfaceVariant)
            }
            .clickable(onClick = onClick),
    ) {
        if (name == null) {
            Row(
                Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Add move", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val on = if (info != null) tint.onContainer else MaterialTheme.colorScheme.onSurface
            Column(Modifier.padding(start = 12.dp, top = 10.dp, end = 34.dp, bottom = 8.dp)) {
                Text(
                    NameFormatter.pretty(name),
                    style = MaterialTheme.typography.titleSmall,
                    color = on,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.weight(1f, fill = false))
                val detail = info?.let {
                    listOfNotNull(
                        it.damageClass?.let(NameFormatter::pretty),
                        it.power?.let { p -> "$p pow" },
                    ).joinToString(" · ")
                }
                Text(
                    text = when {
                        notLearnable -> "Not learnable"
                        detail != null -> detail
                        else -> " "
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (notLearnable) MaterialTheme.colorScheme.error else on.copy(alpha = 0.75f),
                )
            }
            IconButton(onClick = onClear, modifier = Modifier.align(Alignment.TopEnd).size(34.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Clear move", tint = on, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** Searchable list of the moves this species can legally learn, optionally limited to one game. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovePickerSheet(
    options: MemberOptions,
    chosen: Set<String>,
    loadMove: suspend (String) -> MoveInfo?,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var game by remember { mutableStateOf<String?>(null) }
    val groups = remember(options) { Learnset.versionGroups(options.learnset) }
    val legal = remember(options, game) { Learnset.legalMoves(options.learnset, game) }
    val shown = remember(legal, query) {
        val q = query.trim().lowercase().replace('-', ' ')
        if (q.isEmpty()) legal else legal.filter { it.name.replace('-', ' ').contains(q) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Choose a move", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search ${legal.size} moves") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = ChipShape,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
            OakDropdown(
                label = "Learnable in",
                options = listOf(DropdownOption("", "Any game")) +
                    groups.map { DropdownOption(it, NameFormatter.pretty(it)) },
                selected = game ?: "",
                onSelect = { game = it.ifEmpty { null } },
            )

            if (shown.isEmpty()) {
                Hint(
                    if (legal.isEmpty()) "This Pokémon has no moves recorded for that game."
                    else "No move matches \"${query.trim()}\".",
                )
            }

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(shown, key = { it.name }) { move ->
                    val taken = move.name in chosen
                    MoveRow(
                        move = move,
                        taken = taken,
                        loadMove = loadMove,
                        onClick = {
                            onPick(move.name)
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MoveRow(
    move: Learnset.LegalMove,
    taken: Boolean,
    loadMove: suspend (String) -> MoveInfo?,
    onClick: () -> Unit,
) {
    // Rows load their own details as they scroll into view; the repository caches each one.
    val info by produceState<MoveInfo?>(null, move.name) { value = loadMove(move.name) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .let { if (taken) it else it.clickable(onClick = onClick) }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                NameFormatter.pretty(move.name),
                style = MaterialTheme.typography.titleSmall,
                color = if (taken) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = when {
                    taken -> "Already on this Pokémon"
                    else -> move.sources.joinToString(" · ")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        info?.let {
            it.power?.let { p ->
                Text("$p", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            it.type?.let { t -> TypeBadge(type = t, small = true) }
        }
    }
}

/** A move on a team card: a pill in the move's type color (neutral until its type loads). */
@Composable
fun MoveChip(
    name: String,
    loadMove: suspend (String) -> MoveInfo?,
    modifier: Modifier = Modifier,
) {
    val type by produceState<String?>(null, name) { value = loadMove(name)?.type }
    val tint = typeTint(type)
    Text(
        text = NameFormatter.pretty(name),
        style = MaterialTheme.typography.labelMedium,
        color = if (type != null) tint.onContainer else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(ChipShape)
            .background(if (type != null) tint.container else MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, (if (type != null) tint.onContainer else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.2f), ChipShape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}
