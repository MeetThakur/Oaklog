package com.oaklog.app.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.presentation.components.*
import com.oaklog.app.presentation.detail.tabs.*
import com.oaklog.app.presentation.theme.OakFavoriteRed
import com.oaklog.app.presentation.theme.OakShinyGold
import com.oaklog.app.presentation.theme.PokemonTypeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    viewModel: PokemonDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPokemon: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overview", "Stats", "Moves", "Evolution", "Forms", "Location")

    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        when {
            state.isLoading && state.detail == null -> {
                LoadingView(message = "Opening Professor Oak's research journal...")
            }
            state.error != null && state.detail == null -> {
                ErrorView(
                    message = state.error!!,
                    onRetry = { viewModel.loadDetail(state.detail?.id?.toString() ?: "1") }
                )
            }
            state.detail != null -> {
                val detail = state.detail!!
                val isDark = isSystemInDarkTheme()
                val typeGradient = PokemonTypeColors.gradient(detail.types)
                val activeArtwork = if (state.isShiny) {
                    detail.shinyArtworkUrl ?: detail.artworkUrl
                } else {
                    detail.artworkUrl
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header: faint type wash fading into the page background
                    val tint = PokemonTypeColors.color(detail.types.firstOrNull() ?: "unknown")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(tint.copy(alpha = if (isDark) 0.22f else 0.18f), Color.Transparent),
                                )
                            )
                            .padding(bottom = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                HeaderIconButton(
                                    onClick = onNavigateBack,
                                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                                    description = "Back",
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    HeaderIconButton(
                                        onClick = viewModel::toggleShiny,
                                        icon = Icons.Default.AutoAwesome,
                                        description = "Toggle Shiny Artwork",
                                        tint = if (state.isShiny) OakShinyGold else MaterialTheme.colorScheme.onSurface,
                                    )
                                    val isFav = state.collectionEntry?.favorite == true
                                    HeaderIconButton(
                                        onClick = viewModel::toggleFavorite,
                                        icon = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        description = "Favorite",
                                        tint = if (isFav) OakFavoriteRed else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model = activeArtwork,
                                    contentDescription = detail.displayName,
                                    modifier = Modifier.size(200.dp),
                                    contentScale = ContentScale.Fit,
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Column {
                                    Text(
                                        text = NameFormatter.dexNumber(detail.speciesId),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = detail.displayName,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                CollectionStatusButton(
                                    entry = state.collectionEntry,
                                    onCycleStatus = viewModel::cycleCollectionStatus,
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                detail.types.forEach { TypeBadge(type = it) }
                            }
                        }
                    }

                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 16.dp,
                        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline) },
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                    )
                                },
                            )
                        }
                    }

                    // Tab Content
                    when (selectedTabIndex) {
                        0 -> OverviewTab(
                            detail = detail,
                            selectedDexVersion = state.selectedDexVersion,
                            onSelectDexVersion = viewModel::selectDexVersion,
                            abilityInfo = state.abilityInfo,
                        )
                        1 -> StatsTab(detail = detail)
                        2 -> MovesTab(
                            learnset = state.learnset,
                            moveDetails = state.moveDetails,
                            selectedVersionGroup = state.selectedMoveVersionGroup,
                            selectedMethod = state.selectedMoveMethod,
                            onSelectVersionGroup = viewModel::selectMoveVersionGroup,
                            onSelectMethod = viewModel::selectMoveMethod,
                            onExpandMove = viewModel::loadMove,
                        )
                        3 -> EvolutionTab(
                            evolutionRoot = state.evolutionRoot,
                            currentSpeciesId = detail.speciesId,
                            onPokemonClick = onNavigateToPokemon,
                        )
                        4 -> VarietiesTab(
                            varieties = detail.varieties,
                            selectedVarietyName = state.selectedVarietyName,
                            onSelectVariety = viewModel::selectVariety,
                        )
                        5 -> EncountersTab(
                            encounters = state.encounters,
                            selectedVersion = state.selectedEncounterVersion,
                            onSelectVersion = viewModel::selectEncounterVersion,
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
    ) {
        Icon(imageVector = icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
    }
}
