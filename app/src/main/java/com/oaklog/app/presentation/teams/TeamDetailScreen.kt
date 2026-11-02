package com.oaklog.app.presentation.teams

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
                        onEdit = { editingMember = member },
                        onRemove = { viewModel.removeMember(member.id) },
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
                TeamAnalysisView(analysis = analysis)
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
        EditMemberDialog(
            member = member,
            onSave = { updated ->
                viewModel.updateMember(updated)
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

@Composable
private fun TeamMemberCard(
    member: TeamMember,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                model = member.artworkUrl,
                contentDescription = member.pokemonName,
                modifier = Modifier.size(54.dp),
                contentScale = ContentScale.Fit,
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = member.nickname ?: NameFormatter.pretty(member.pokemonName),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (member.nickname != null) {
                        Text(
                            text = "(${NameFormatter.pretty(member.pokemonName)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    member.types.forEach { type ->
                        TypeBadge(type = type, small = true)
                    }
                }

                if (member.ability != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Ability: ${NameFormatter.pretty(member.ability)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (member.moves.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Moves: ${member.moves.joinToString(", ") { NameFormatter.pretty(it) }}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Member",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Member",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EditMemberDialog(
    member: TeamMember,
    onSave: (TeamMember) -> Unit,
    onDismiss: () -> Unit,
) {
    var nickname by remember { mutableStateOf(member.nickname ?: "") }
    var ability by remember { mutableStateOf(member.ability ?: "") }
    var movesText by remember { mutableStateOf(member.moves.joinToString(", ")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${NameFormatter.pretty(member.pokemonName)}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname (optional)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = ability,
                    onValueChange = { ability = it },
                    label = { Text("Ability") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = movesText,
                    onValueChange = { movesText = it },
                    label = { Text("Moves (up to 4, comma separated)") },
                    supportingText = { Text("e.g. Thunderbolt, Quick Attack, Iron Tail") },
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val movesList = movesText.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .take(4)
                onSave(
                    member.copy(
                        nickname = nickname.trim().ifEmpty { null },
                        ability = ability.trim().ifEmpty { null },
                        moves = movesList,
                    )
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
