package com.oaklog.app.presentation.navigation

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OakLogBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Only show bottom bar on the four root tabs
    val isBottomBarVisible = bottomNavItems.any { it.screen.route == currentRoute }
    if (!isBottomBarVisible) return

    Column(modifier = modifier) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
    ShortNavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            ShortNavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                    )
                },
                label = { Text(text = item.title) },
                colors = ShortNavigationBarItemDefaults.colors(
                    selectedIndicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
    }
}
