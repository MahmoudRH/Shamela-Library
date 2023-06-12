package com.shamela.library.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.shamela.library.presentation.screens.download.DownloadScreen
import com.shamela.library.presentation.screens.favorite.FavoriteScreen
import com.shamela.library.presentation.screens.library.LibraryScreen
import com.shamela.library.presentation.screens.search.SearchScreen
import com.shamela.library.presentation.screens.settings.SettingsScreen

sealed class HomeHostDestination(
    val route: String,
    val unSelectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String = "",
) {
    object Library : HomeHostDestination(
        "LIBRARY_SCREEN",
        Icons.Outlined.LocalLibrary,
        Icons.Default.LocalLibrary,
        "المكتبة"
    )

    object Download : HomeHostDestination(
        "DOWNLOAD_SCREEN",
        Icons.Outlined.DownloadForOffline,
        Icons.Default.DownloadForOffline,
        "التحميل"
    )

    object Favorite : HomeHostDestination(
        "FAVORITE_SCREEN",
        Icons.Outlined.FavoriteBorder,
        Icons.Default.Favorite,
        "المفضلة"
    )

    object Search : HomeHostDestination(
        "SEARCH_SCREEN",
        Icons.Outlined.Search,
        Icons.Default.Search,
        "البحث"
    )

    object Settings : HomeHostDestination(
        "SETTINGS_SCREEN",
        Icons.Outlined.Settings,
        Icons.Default.Settings,
        "الإعدادات"
    )
}

fun NavGraphBuilder.homeGraph(navController: NavController) {
    navigation(
        startDestination = HomeHostDestination.Library.route,
        route = NavigationGraphs.HOME_GRAPH_ROUTE
    ) {
        composable(HomeHostDestination.Library.route) { LibraryScreen() }
        composable(HomeHostDestination.Download.route) { DownloadScreen() }
        composable(HomeHostDestination.Favorite.route) { FavoriteScreen() }
        composable(HomeHostDestination.Search.route) { SearchScreen() }
        composable(HomeHostDestination.Settings.route) { SettingsScreen() }
    }
}
