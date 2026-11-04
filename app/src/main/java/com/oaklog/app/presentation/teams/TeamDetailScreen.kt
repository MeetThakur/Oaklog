package com.oaklog.app.presentation.teams

import com.oaklog.app.presentation.components.Card
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.presentation.theme.typeTint
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.domain.model.MoveInfo
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape
import com.oaklog.app.domain.logic.SpeedTierEntry
import com.oaklog.app.domain.model.Team
import com.oaklog.app.domain.model.TeamMember
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.components.TypeBadge

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

    if (team == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("Team not found")
        }
        return
    }

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
                        Icon(Icons.Default.Edit, contentDescription = "Rename Team")
                    }
                    IconButton(onClick = {
                        viewModel.deleteTeam(team.id)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete Team")
                    }
                }
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Team Members Section (6 Slots)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Team Members (${team.members.size}/6)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                team.members.forEach { member ->
                    TeamMemberCard(
                        member = member,
                        loadMove = viewModel::moveInfo,
                        onEdit = { editingMember = member },
                    )
                }

                if (team.members.size < 6) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showPickerSheet = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Add Pokémon (${6 - team.members.size} slots open)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            // Real-time Team Analysis Section
            val analysis = viewModel.computeAnalysis(team, state.typeChart)
            if (analysis != null && team.members.isNotEmpty()) {
                // Speed needs each member's detail payload, so it fills in after the type-only cards.
                val memberKey = team.members.map { it.id to it.pokemonId }
                val speedTiers by produceState(emptyList<SpeedTierEntry>(), memberKey) {
                    value = viewModel.speedTiers(team)
                }
                TeamAnalysisView(
                    analysis = analysis,
                    suggestions = viewModel.suggestions(team, analysis, state.typeChart),
                    speedTiers = speedTiers,
                )
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
        // Pair a "finished loading" flag with the result so a failure is distinguishable from loading.
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
            title = { Text("Rename Team") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Team Name") },
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.renameTeam(team.id, renameText)
                    showRenameDialog = false
                }) {
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

/**
 * A member in its own type color: art, name, types, ability, and all four moves as pills colored by
 * the move's type. The whole card opens the editor.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeamMemberCard(
    member: TeamMember,
    loadMove: suspend (String) -> MoveInfo?,
    onEdit: () -> Unit,
) {
    val tint = typeTint(member.types.firstOrNull())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(tint.container)
            .clickable(onClick = onEdit)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.nickname ?: NameFormatter.pretty(member.pokemonName),
                    style = MaterialTheme.typography.titleMedium,
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
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit ${NameFormatter.pretty(member.pokemonName)}",
                tint = tint.onContainer,
                modifier = Modifier.size(20.dp),
            )
        }

        Text(
            text = member.ability?.let { "Ability  ${NameFormatter.pretty(it)}" } ?: "No ability chosen",
            style = MaterialTheme.typography.labelLarge,
            color = tint.onContainer.copy(alpha = if (member.ability != null) 1f else 0.65f),
        )

        if (member.moves.isEmpty()) {
            Text(
                text = "No moves yet. Tap to choose up to four.",
                style = MaterialTheme.typography.bodySmall,
                color = tint.onContainer.copy(alpha = 0.7f),
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                member.moves.forEach { MoveChip(name = it, loadMove = loadMove) }
            }
        }
    }
}
