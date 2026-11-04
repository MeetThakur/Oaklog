package com.oaklog.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oaklog.app.presentation.collection.CollectionScreen
import com.oaklog.app.presentation.collection.CollectionViewModel
import com.oaklog.app.presentation.detail.PokemonDetailScreen
import com.oaklog.app.presentation.detail.PokemonDetailViewModel
import com.oaklog.app.presentation.explore.ExploreScreen
import com.oaklog.app.presentation.explore.ExploreViewModel
import com.oaklog.app.presentation.explore.tools.*
import com.oaklog.app.presentation.pokedex.PokedexScreen
import com.oaklog.app.presentation.pokedex.PokedexViewModel
import com.oaklog.app.presentation.settings.SettingsScreen
import com.oaklog.app.presentation.settings.SettingsViewModel
import com.oaklog.app.presentation.teams.TeamDetailScreen
import com.oaklog.app.presentation.teams.TeamsScreen
import com.oaklog.app.presentation.teams.TeamsViewModel

@Composable
fun OakLogNavHost(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            OakLogBottomBar(navController = navController)
        },
        contentWindowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Pokedex.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
            },
        ) {
            // ---- TAB 1: POKÉDEX ---------------------------------------------------------------
            composable(Screen.Pokedex.route) {
                val viewModel: PokedexViewModel = hiltViewModel()
                PokedexScreen(
                    viewModel = viewModel,
                    onPokemonClick = { pokemonId ->
                        navController.navigate(Screen.PokemonDetail.createRoute(pokemonId))
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    },
                )
            }

            // ---- TAB 2: TEAMS -----------------------------------------------------------------
            composable(Screen.Teams.route) {
                val viewModel: TeamsViewModel = hiltViewModel()
                TeamsScreen(
                    viewModel = viewModel,
                    onTeamClick = { teamId ->
                        navController.navigate(Screen.TeamDetail.createRoute(teamId))
                    },
                )
            }

            // ---- TAB 3: COLLECTION ------------------------------------------------------------
            composable(Screen.Collection.route) {
                val viewModel: CollectionViewModel = hiltViewModel()
                CollectionScreen(
                    viewModel = viewModel,
                    onPokemonClick = { pokemonId ->
                        navController.navigate(Screen.PokemonDetail.createRoute(pokemonId))
                    },
                )
            }

            // ---- TAB 4: EXPLORE ---------------------------------------------------------------
            composable(Screen.Explore.route) {
                val viewModel: ExploreViewModel = hiltViewModel()
                ExploreScreen(
                    viewModel = viewModel,
                    onNavigateToTypeChart = { navController.navigate(Screen.TypeChart.route) },
                    onNavigateToMoves = { navController.navigate(Screen.MovesDatabase.route) },
                    onNavigateToAbilities = { navController.navigate(Screen.AbilitiesDatabase.route) },
                    onNavigateToItems = { navController.navigate(Screen.ItemsDatabase.route) },
                    onNavigateToRegions = { navController.navigate(Screen.Regions.route) },
                )
            }

            // ---- POKÉMON DETAIL ---------------------------------------------------------------
            composable(
                route = Screen.PokemonDetail.route,
                arguments = listOf(navArgument("pokemonId") { type = NavType.StringType }),
            ) {
                val viewModel: PokemonDetailViewModel = hiltViewModel()
                PokemonDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPokemon = { nextId ->
                        navController.navigate(Screen.PokemonDetail.createRoute(nextId))
                    },
                )
            }

            // ---- TEAM DETAIL ------------------------------------------------------------------
            composable(
                route = Screen.TeamDetail.route,
                arguments = listOf(navArgument("teamId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L
                val viewModel: TeamsViewModel = hiltViewModel()
                TeamDetailScreen(
                    teamId = teamId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            // ---- EXPLORE TOOLS ----------------------------------------------------------------
            composable(Screen.TypeChart.route) {
                val viewModel: ExploreViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()
                TypeChartScreen(
                    typeChart = state.typeChart,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(Screen.MovesDatabase.route) {
                val viewModel: ExploreViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()
                MovesDatabaseScreen(
                    moves = state.movesList,
                    isLoading = state.isLoading,
                    onSearch = viewModel::searchMove,
                    onInit = viewModel::loadInitialMoves,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(Screen.AbilitiesDatabase.route) {
                val viewModel: ExploreViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()
                AbilitiesDatabaseScreen(
                    abilities = state.abilitiesList,
                    isLoading = state.isLoading,
                    onSearch = viewModel::searchAbility,
                    onInit = viewModel::loadInitialAbilities,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(Screen.ItemsDatabase.route) {
                val viewModel: ExploreViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()
                ItemsDatabaseScreen(
                    items = state.itemsList,
                    isLoading = state.isLoading,
                    onSearch = viewModel::searchItem,
                    onInit = viewModel::loadInitialItems,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(Screen.Regions.route) {
                val viewModel: ExploreViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()
                RegionsScreen(
                    regions = state.regions,
                    generations = state.generations,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            // ---- SETTINGS ---------------------------------------------------------------------
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}

