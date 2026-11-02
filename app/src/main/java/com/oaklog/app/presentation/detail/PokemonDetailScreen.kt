package com.oaklog.app.presentation.detail

import androidx.compose.foundation.background
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
    val tabTitles = listOf("Overview", "Stats", "Evolution", "Forms", "Location")

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
                    // Header with Type Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(typeGradient)
                            .padding(bottom = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            // Navigation & Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = onNavigateBack,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.2f)),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Shiny toggle
                                    IconButton(
                                        onClick = viewModel::toggleShiny,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (state.isShiny) OakShinyGold.copy(alpha = 0.3f)
                                                else Color.Black.copy(alpha = 0.2f)
                                            ),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Toggle Shiny Artwork",
                                            tint = if (state.isShiny) OakShinyGold else Color.White,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }

                                    // Favorite toggle
                                    IconButton(
                                        onClick = viewModel::toggleFavorite,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.2f)),
                                    ) {
                                        Icon(
                                            imageVector = if (state.collectionEntry?.favorite == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (state.collectionEntry?.favorite == true) OakFavoriteRed else Color.White,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // Name, Number, and Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = NameFormatter.dexNumber(detail.speciesId),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                    )
                                    Text(
                                        text = detail.displayName,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }

                                CollectionStatusButton(
                                    entry = state.collectionEntry,
                                    onCycleStatus = viewModel::cycleCollectionStatus,
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            // Type Badges
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                detail.types.forEach { type ->
                                    TypeBadge(type = type)
                                }
                            }

                            // Large Artwork
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model = activeArtwork,
                                    contentDescription = detail.displayName,
                                    modifier = Modifier.size(190.dp),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        }
                    }

                    // Secondary Navigation Tabs
                    PrimaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
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
                        )
                        1 -> StatsTab(detail = detail)
                        2 -> EvolutionTab(
                            evolutionRoot = state.evolutionRoot,
                            currentSpeciesId = detail.speciesId,
                            onPokemonClick = onNavigateToPokemon,
                        )
                        3 -> VarietiesTab(
                            varieties = detail.varieties,
                            selectedVarietyName = state.selectedVarietyName,
                            onSelectVariety = viewModel::selectVariety,
                        )
                        4 -> EncountersTab(
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
