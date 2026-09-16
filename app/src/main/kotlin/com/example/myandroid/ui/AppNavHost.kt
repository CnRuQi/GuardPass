package com.example.myandroid.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myandroid.ui.category.CategoryScreenV2
import com.example.myandroid.ui.component.AppScaffold
import com.example.myandroid.ui.entry.EntryEditorScreen
import com.example.myandroid.ui.search.SearchScreenV2
import com.example.myandroid.ui.settings.SettingsScreenV2
import com.example.myandroid.ui.vault.VaultScreen
import com.example.myandroid.ui.vault.VaultViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    fun navigateToRoot(route: String) {
        navController.navigate(route) {
            popUpTo(AppRoute.Vault.path) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(navController = navController, startDestination = AppRoute.Vault.path) {
        composable(AppRoute.Vault.path) {
            AppScaffold(
                selectedRoute = AppRoute.Vault.path,
                onRouteSelected = ::navigateToRoot
            ) { padding ->
                VaultScreen(
                    contentPadding = padding,
                    onOpenSearch = { navController.navigate(AppRoute.Search.path) },
                    onOpenEditor = { id -> navController.navigate(AppRoute.Editor(id).path) }
                )
            }
        }
        composable(AppRoute.Search.path) {
            SearchScreenV2(
                onBack = { navController.popBackStack() },
                onOpenEditor = { id -> navController.navigate(AppRoute.Editor(id).path) }
            )
        }
        composable(AppRoute.Favorites.path) {
            AppScaffold(
                selectedRoute = AppRoute.Favorites.path,
                onRouteSelected = ::navigateToRoot
            ) { padding ->
                VaultScreen(
                    contentPadding = padding,
                    initialCategoryId = VaultViewModel.FAVORITES_FILTER,
                    screenTitle = "收藏",
                    onOpenSearch = { navController.navigate(AppRoute.Search.path) },
                    onOpenEditor = { id -> navController.navigate(AppRoute.Editor(id).path) }
                )
            }
        }
        composable(AppRoute.Editor.NEW_EDITOR_PATH) {
            EntryEditorScreen(
                entryId = null,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = AppRoute.Editor.EDITOR_ARGUMENT_PATH,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            EntryEditorScreen(
                entryId = backStackEntry.arguments?.getLong("entryId"),
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppRoute.Categories.path) {
            AppScaffold(
                selectedRoute = AppRoute.Categories.path,
                onRouteSelected = ::navigateToRoot
            ) { padding ->
                CategoryScreenV2(contentPadding = padding)
            }
        }
        composable(AppRoute.Settings.path) {
            AppScaffold(
                selectedRoute = AppRoute.Settings.path,
                onRouteSelected = ::navigateToRoot
            ) { padding ->
                SettingsScreenV2(contentPadding = padding)
            }
        }
    }
}
