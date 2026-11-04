package com.oaklog.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Group
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Pokedex : Screen("pokedex")
    data object Teams : Screen("teams")
    data object Collection : Screen("collection")
    data object Explore : Screen("explore")

    data object PokemonDetail : Screen("pokemon/{pokemonId}") {
        fun createRoute(pokemonId: Int): String = "pokemon/$pokemonId"
    }

    data object TeamDetail : Screen("team/{teamId}") {
        fun createRoute(teamId: Long): String = "team/$teamId"
    }

    data object TypeChart : Screen("explore/type_chart")
    data object MovesDatabase : Screen("explore/moves")
    data object AbilitiesDatabase : Screen("explore/abilities")
    data object ItemsDatabase : Screen("explore/items")
    data object Regions : Screen("explore/regions")
    data object EvolutionFinder : Screen("explore/evolutions")

    data object RegionPlaces : Screen("explore/region/{name}") {
        fun createRoute(name: String) = "explore/region/$name"
    }

    data object LocationDetail : Screen("explore/location/{name}") {
        fun createRoute(name: String) = "explore/location/$name"
    }
    data object Settings : Screen("settings")
    data object Search : Screen("search")
    data object Calculator : Screen("explore/calculator")

    /** Both slots are optional; opening from a detail page pre-fills the first. */
    data object Compare : Screen("compare?a={a}&b={b}") {
        fun createRoute(a: Int? = null, b: Int? = null): String = buildString {
            append("compare")
            val params = listOfNotNull(a?.let { "a=$it" }, b?.let { "b=$it" })
            if (params.isNotEmpty()) append("?").append(params.joinToString("&"))
        }
    }
}

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Pokedex,
        title = "Pokédex",
        selectedIcon = Icons.AutoMirrored.Filled.ListAlt,
        unselectedIcon = Icons.AutoMirrored.Outlined.ListAlt,
    ),
    BottomNavItem(
        screen = Screen.Teams,
        title = "Teams",
        selectedIcon = Icons.Filled.Group,
        unselectedIcon = Icons.Outlined.Group,
    ),
    BottomNavItem(
        screen = Screen.Explore,
        title = "Explore",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
    ),
)
