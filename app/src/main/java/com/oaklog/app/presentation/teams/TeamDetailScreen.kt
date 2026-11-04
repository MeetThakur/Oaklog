package com.oaklog.app.presentation.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.CoverageResult
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.logic.SpeedTierEntry
import com.oaklog.app.domain.model.MoveInfo
import com.oaklog.app.domain.model.Team
import com.oaklog.app.domain.model.TeamMember
import com.oaklog.app.presentation.components.Card
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.components.TypeBadge
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.ChipShape
import com.oaklog.app.presentation.theme.typeTint
import java.util.Collections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamId: Long,
    viewModel: TeamsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val team = state.teams.firstOrNull { it.id == teamId }
    var showPickerSheet by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<TeamMember?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    if (team == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("Squad not found")
        }
        return
    }

    // Short labels so every tab stays on one line; the board above already shows the member count.
    val tabs = listOf("Squad", "Synergy", "Speed", "Advice")

    Scaffold(
        topBar = {
            OakTopBar(
                title = team.name,
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        renameText = team.name
                        showRenameDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename Squad")
                    }
                    IconButton(onClick = {
                        viewModel.deleteTeam(team.id)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete Squad")
                    }
                }
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Squad board: six tinted slots plus type coverage
            // Recomputed whenever members, their moves, or the type chart change.
            val coverageKey = team.members.map { Triple(it.id, it.moves, it.nickname) }
            val coverage by produceState<CoverageResult?>(null, coverageKey, state.typeChart) {
                value = viewModel.coverage(team)
            }
            SquadBoard(
                team = team,
                analysis = viewModel.computeAnalysis(team, state.typeChart),
                coverage = coverage,
                onAddMember = { showPickerSheet = true },
                onSelectMember = { member ->
                    selectedTab = 0
                    editingMember = member
                },
            )

            // Primary M3 Tab Row
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                            )
                        },
                    )
                }
            }

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> {
                        // Squad Members Tab
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            team.members.forEachIndexed { index, member ->
                                TeamMemberCard(
                                    member = member,
                                    slotIndex = index + 1,
                                    canMoveUp = index > 0,
                                    canMoveDown = index < team.members.size - 1,
                                    onMoveUp = {
                                        val list = team.members.map { it.id }.toMutableList()
                                        Collections.swap(list, index, index - 1)
                                        viewModel.reorderMembers(team.id, list)
                                    },
                                    onMoveDown = {
                                        val list = team.members.map { it.id }.toMutableList()
                                        Collections.swap(list, index, index + 1)
                                        viewModel.reorderMembers(team.id, list)
                                    },
                                    loadMove = viewModel::moveInfo,
                                    onEdit = { editingMember = member },
                                    onRemove = { viewModel.removeMember(member.id) },
                                )
                            }

                            if (team.members.size < 6) {
                                AddMemberSlotCard(
                                    slotsRemaining = 6 - team.members.size,
                                    nextSlot = team.members.size + 1,
                                    onClick = { showPickerSheet = true },
                                )
                            }
                        }
                    }
                    1 -> {
                        // Synergy Tab
                        val analysis = viewModel.computeAnalysis(team, state.typeChart)
                        if (analysis != null && team.members.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                            ) {
                                TeamSynergyTab(analysis = analysis)
                            }
                        } else {
                            EmptySquadState("Add squad members to analyze defensive synergies and STAB coverage.")
                        }
                    }
                    2 -> {
                        // Speed Tiers Tab
                        if (team.members.isNotEmpty()) {
                            val memberKey = team.members.map { it.id to it.pokemonId }
                            val speedTiers by produceState(emptyList<SpeedTierEntry>(), memberKey) {
                                value = viewModel.speedTiers(team)
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                            ) {
                                TeamSpeedTiersTab(tiers = speedTiers)
                            }
                        } else {
                            EmptySquadState("Add squad members to evaluate speed rankings and turn orders.")
                        }
                    }
                    3 -> {
                        // Professor Oak's Advice Tab
                        val analysis = viewModel.computeAnalysis(team, state.typeChart)
                        if (analysis != null && team.members.isNotEmpty()) {
                            val tips = viewModel.suggestions(team, analysis, state.typeChart)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                            ) {
                                TeamOakAdviceTab(suggestions = tips)
                            }
                        } else {
                            EmptySquadState("Add squad members to receive Professor Oak's field evaluation.")
                        }
                    }
                }
            }
        }
    }

    if (showPickerSheet) {
        PokemonPickerSheet(
            pokemonList = state.allPokemon,
            onSelectPokemon = { pokemon ->
                viewModel.addMember(team.id, pokemon)
            },
            onDismiss = { showPickerSheet = false },
        )
    }

    editingMember?.let { member ->
        val loaded by produceState(false to (null as MemberOptions?), member.id) {
            value = true to viewModel.memberOptions(member)
        }
        MemberEditorSheet(
            member = member,
            options = loaded.second,
            optionsFailed = loaded.first && loaded.second == null,
            loadMove = viewModel::moveInfo,
            onSave = { updated ->
                viewModel.updateMember(updated)
                editingMember = null
            },
            onRemove = {
                viewModel.removeMember(member.id)
                editingMember = null
            },
            onDismiss = { editingMember = null },
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Squad", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Squad Name") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameTeam(team.id, renameText)
                        showRenameDialog = false
                    },
                    shape = ChipShape,
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmptySquadState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Squad Incomplete",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * High-profile Trainer Card for each squad member.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeamMemberCard(
    member: TeamMember,
    slotIndex: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    loadMove: suspend (String) -> MoveInfo?,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    val tint = typeTint(member.types.firstOrNull())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(tint.container)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top Row: Slot Badge, Number, Reorder buttons & Remove
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Surface(
                    shape = ChipShape,
                    color = tint.onContainer.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = "SLOT $slotIndex",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tint.onContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }

                Text(
                    text = NameFormatter.dexNumber(member.pokemonId),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = tint.onContainer.copy(alpha = 0.7f),
                )
            }

            // Quick Reorder Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move Up",
                        tint = if (canMoveUp) tint.onContainer else tint.onContainer.copy(alpha = 0.25f),
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Move Down",
                        tint = if (canMoveDown) tint.onContainer else tint.onContainer.copy(alpha = 0.25f),
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Remove Member",
                        tint = tint.onContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        // Main Member Presentation Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Circular Art Disc
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(tint.art),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = member.artworkUrl,
                    contentDescription = member.pokemonName,
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.nickname ?: NameFormatter.pretty(member.pokemonName),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = tint.onContainer,
                )
                if (member.nickname != null) {
                    Text(
                        text = NameFormatter.pretty(member.pokemonName),
                        style = MaterialTheme.typography.bodySmall,
                        color = tint.onContainer.copy(alpha = 0.75f),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    member.types.forEach { TypeBadge(type = it, small = true, onTint = tint) }
                }
            }
        }

        // Ability Chip
        Surface(
            shape = ChipShape,
            color = tint.onContainer.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = tint.onContainer,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = member.ability?.let { "Ability: ${NameFormatter.pretty(it)}" } ?: "No ability assigned",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tint.onContainer,
                )
            }
        }

        // 4 Move Slots in 2x2 Grid
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (row in 0 until 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    for (col in 0 until 2) {
                        val slotIndex = row * 2 + col
                        val moveName = member.moves.getOrNull(slotIndex)
                        MemberMovePill(
                            name = moveName,
                            loadMove = loadMove,
                            onTint = tint,
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // Action: Configure
        OutlinedButton(
            onClick = onEdit,
            shape = ChipShape,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = tint.onContainer,
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = androidx.compose.ui.graphics.SolidColor(tint.onContainer.copy(alpha = 0.25f))
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Configure Moves & Ability",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun MemberMovePill(
    name: String?,
    loadMove: suspend (String) -> MoveInfo?,
    onTint: com.oaklog.app.presentation.theme.TypeTint,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (name == null) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = onTint.onContainer.copy(alpha = 0.05f),
            modifier = modifier
                .height(40.dp)
                .clickable(onClick = onClick),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(
                    text = "+ Move",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = onTint.onContainer.copy(alpha = 0.45f),
                )
            }
        }
    } else {
        val moveInfo by produceState<MoveInfo?>(null, name) {
            value = loadMove(name)
        }
        val moveTint = typeTint(moveInfo?.type)

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = moveTint.container,
            modifier = modifier
                .height(40.dp)
                .clickable(onClick = onClick),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = NameFormatter.pretty(name),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = moveTint.onContainer,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false),
                )
                moveInfo?.power?.let { power ->
                    Text(
                        text = "$power",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = moveTint.onContainer.copy(alpha = 0.75f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddMemberSlotCard(
    slotsRemaining: Int,
    nextSlot: Int,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Add Pokémon to Slot $nextSlot",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "$slotsRemaining ${if (slotsRemaining == 1) "slot" else "slots"} remaining in this squad",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
