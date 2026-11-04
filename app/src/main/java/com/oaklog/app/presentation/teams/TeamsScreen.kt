package com.oaklog.app.presentation.teams

import androidx.compose.material.icons.outlined.Groups
import com.oaklog.app.presentation.components.Card
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.oaklog.app.domain.model.Team
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.components.TypeBadge

@Composable
fun TeamsScreen(
    viewModel: TeamsViewModel,
    onTeamClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTeamName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            OakTopBar(title = "Team Builder")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newTeamName = "Research Team ${state.teams.size + 1}"
                    showCreateDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Team")
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.teams.isEmpty()) {
                EmptyView(
                    title = "Build your first team",
                    subtitle = "Pick up to six Pokémon, then see shared weaknesses, coverage gaps and speed tiers.",
                    icon = Icons.Outlined.Groups,
                    actionLabel = "Create a team",
                    onAction = {
                        newTeamName = "Research Team ${state.teams.size + 1}"
                        showCreateDialog = true
                    },
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.teams, key = { it.id }) { team ->
                        TeamCard(
                            team = team,
                            onClick = { onTeamClick(team.id) },
                            onDelete = { viewModel.deleteTeam(team.id) },
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Pokémon Team") },
            text = {
                OutlinedTextField(
                    value = newTeamName,
                    onValueChange = { newTeamName = it },
                    label = { Text("Team Name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.createTeam(newTeamName) { newId ->
                        showCreateDialog = false
                        onTeamClick(newId)
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TeamCard(
    team: Team,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = team.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${team.members.size} / 6 Pokémon",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete Team",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Row of member sprites / placeholders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (i in 0 until 6) {
                    val member = team.members.getOrNull(i)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ) {
                        if (member != null) {
                            Box(contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    model = member.artworkUrl,
                                    contentDescription = member.pokemonName,
                                    modifier = Modifier.size(40.dp),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        }
                    }
                }
            }

            // Types represented
            val types = team.members.flatMap { it.types }.distinct()
            if (types.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    types.take(6).forEach { type ->
                        TypeBadge(type = type, small = true)
                    }
                    if (types.size > 6) {
                        Text(
                            text = "+${types.size - 6}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically),
                        )
                    }
                }
            }
        }
    }
}

