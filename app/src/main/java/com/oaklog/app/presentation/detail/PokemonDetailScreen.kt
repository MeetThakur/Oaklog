package com.oaklog.app.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.presentation.components.*
import com.oaklog.app.presentation.detail.tabs.*
import com.oaklog.app.presentation.navigation.sharedArtwork
import com.oaklog.app.presentation.theme.OakFavoriteRed
import com.oaklog.app.presentation.theme.OakShinyGold
import com.oaklog.app.presentation.theme.PokemonTypeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    viewModel: PokemonDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPokemon: (Int) -> Unit,
    onCompare: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var reference by remember { mutableStateOf<ReferenceTarget?>(null) }
    val playCry = rememberCryPlayer()
    val tabTitles = listOf("Overview", "Stats", "Moves", "Evolution", "Forms", "Location")

    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        when {
            state.isLoading && state.detail == null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Same artwork as the list card, so the shared-element morph has a target at once.
                    viewModel.initialSpeciesId?.let { id ->
                        AsyncImage(
                            model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png",
                            contentDescription = null,
                            modifier = Modifier.size(200.dp).sharedArtwork(id),
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Text(
                        text = "Opening Professor Oak's research journal...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            state.error != null && state.detail == null -> {
                ErrorView(
                    message = state.error!!,
                    onRetry = { viewModel.loadDetail(state.detail?.id?.toString() ?: "1") }
                )
            }
            state.detail != null -> {
                val detail = state.detail!!
                val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                val typeGradient = PokemonTypeColors.gradient(detail.types)
                // Official artwork has no female variant; those forms fall back to the in-game sprite.
                val showFemale = state.isFemale && detail.femaleSpriteUrl != null
                val activeArtwork = when {
                    showFemale && state.isShiny -> detail.shinyFemaleSpriteUrl ?: detail.femaleSpriteUrl
                    showFemale -> detail.femaleSpriteUrl
                    state.isShiny -> detail.shinyArtworkUrl ?: detail.artworkUrl
                    else -> detail.artworkUrl
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
                                        onClick = { onCompare(detail.speciesId) },
                                        icon = Icons.Outlined.CompareArrows,
                                        description = "Compare",
                                    )
                                    if (detail.femaleSpriteUrl != null) {
                                        HeaderIconButton(
                                            onClick = viewModel::toggleFemale,
                                            icon = if (state.isFemale) Icons.Default.Female else Icons.Default.Male,
                                            description = if (state.isFemale) "Showing female, tap for male" else "Showing male, tap for female",
                                        )
                                    }
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
                                    modifier = Modifier
                                        .size(200.dp)
                                        .sharedArtwork(detail.speciesId)
                                        .clickable(enabled = detail.criesUrl != null) { playCry(detail.criesUrl!!) },
                                    contentScale = ContentScale.Fit,
                                    filterQuality = if (showFemale) FilterQuality.None else FilterQuality.Low,
                                )
                                if (detail.criesUrl != null) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Play cry",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(20.dp),
                                    )
                                }
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
                            onAbilityClick = { reference = ReferenceTarget.Ability(it) },
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
                            onOpenMove = { reference = ReferenceTarget.Move(it) },
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

    reference?.let { target ->
        ReferenceSheet(
            target = target,
            onDismiss = { reference = null },
            onOpenPokemon = onNavigateToPokemon,
        )
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
