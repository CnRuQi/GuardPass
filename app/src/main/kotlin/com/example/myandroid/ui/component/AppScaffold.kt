package com.example.myandroid.ui.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myandroid.R
import com.example.myandroid.ui.AppRoute

private data class NavigationItem(
    val route: String,
    val label: String,
    val icon: Int
)

private val navigationItems = listOf(
    NavigationItem(AppRoute.Vault.path, "密码库", R.drawable.ic_home),
    NavigationItem(AppRoute.Favorites.path, "收藏", R.drawable.ic_star_filled),
    NavigationItem(AppRoute.Categories.path, "分类", R.drawable.ic_category),
    NavigationItem(AppRoute.Settings.path, "设置", R.drawable.ic_settings)
)

@Composable
fun AppScaffold(
    selectedRoute: String,
    onRouteSelected: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 600.dp
        if (expanded) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        navigationItems.forEach { item ->
                            NavigationRailItem(
                                selected = selectedRoute == item.route,
                                onClick = { onRouteSelected(item.route) },
                                icon = {
                                    Icon(
                                        painter = painterResource(item.icon),
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
                Scaffold(
                    modifier = Modifier.weight(1f),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { padding ->
                    content(padding)
                }
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    NavigationBar {
                        navigationItems.forEach { item ->
                            NavigationBarItem(
                                selected = selectedRoute == item.route,
                                onClick = { onRouteSelected(item.route) },
                                icon = {
                                    Icon(
                                        painter = painterResource(item.icon),
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            ) { padding ->
                content(padding)
            }
        }
    }
}
