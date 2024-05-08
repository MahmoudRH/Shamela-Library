package com.shamela.library.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.shamela.apptheme.presentation.settings.PreferenceSettingsScreen
import com.shamela.library.presentation.screens.download.DownloadScreen
import com.shamela.library.presentation.screens.favorite.FavoriteScreen
import com.shamela.library.presentation.screens.library.LibraryScreen
import com.shamela.library.presentation.screens.search.SearchScreen
import com.shamela.library.presentation.screens.searchResults.SearchResultsScreen
import com.shamela.library.presentation.screens.sectionBooks.SectionBooksScreen
import com.shamela.library.presentation.screens.settings.SettingsScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface HomeHostDestination {
    val route: String
    val unSelectedIcon: ImageVector
    val selectedIcon: ImageVector
    val actionIcon: ImageVector?
    val onActionClick: () -> Boolean
    val label: String
}

object Library : HomeHostDestination {
    override val route = "LIBRARY_SCREEN"
    override val unSelectedIcon = Icons.Outlined.LocalLibrary
    override val selectedIcon = Icons.Default.LocalLibrary
    override val actionIcon = Icons.Outlined.Search
    override val label = "المكتبة"
    private val _buttons = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val buttons: Flow<Boolean> = _buttons.asSharedFlow()
    override val onActionClick = {
        _buttons.tryEmit(true)
    }
}

object Download : HomeHostDestination {
    override val route = "DOWNLOAD_SCREEN"
    override val unSelectedIcon = Icons.Outlined.FileDownload
    override val selectedIcon = Icons.Default.FileDownload
    override val actionIcon = Icons.Outlined.Search
    override val label = "التحميل"

    private val _buttons = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val buttons: Flow<Boolean> = _buttons.asSharedFlow()
    override val onActionClick = {
        _buttons.tryEmit(true)
    }

}

object Favorite : HomeHostDestination {
    override val route = "FAVORITE_SCREEN"
    override val unSelectedIcon = Icons.Outlined.FavoriteBorder
    override val selectedIcon = Icons.Default.Favorite
    override val label = "المفضلة"
    override val actionIcon = null
    override val onActionClick = {
        false
    }

}

object Search : HomeHostDestination {
    override val route = "SEARCH_SCREEN"
    override val unSelectedIcon = Icons.Outlined.Search
    override val selectedIcon = Icons.Default.Search
    override val label = "البحث"
    override val actionIcon = null
    override val onActionClick = {
        false
    }
}

object Settings : HomeHostDestination {
    override val route = "SETTINGS_SCREEN"
    override val unSelectedIcon = Icons.Outlined.Settings
    override val selectedIcon = Icons.Default.Settings
    override val label = "الإعدادات"
    override val actionIcon = null
    override val onActionClick = {
        false
    }
}

object SectionBooks : HomeHostDestination {
    override val route = "SECTION_BOOKS/{categoryName}/{type}"
    override val unSelectedIcon = Icons.Outlined.Book
    override val selectedIcon = Icons.Outlined.Book
    override val label = ""
    override val actionIcon = null
    override val onActionClick = {
        false
    }

    fun createRoute(categoryName: String, type: String): String {
        return "SECTION_BOOKS/$categoryName/$type"
    }
}

object SearchResults : HomeHostDestination {
    override val route = "SEARCH_RESULTS/{categoryName}/{type}"
    override val unSelectedIcon = Icons.Outlined.Book
    override val selectedIcon = Icons.Outlined.Book
    override val label = ""
    override val actionIcon = null
    override val onActionClick = {
        false
    }

    fun createRoute(categoryName: String, type: String): String {
        return "SEARCH_RESULTS/$categoryName/$type"
    }
}


fun NavGraphBuilder.homeGraph(navController: NavController) {
    navigation(
        startDestination = Library.route,
        route = NavigationGraphs.HOME_GRAPH_ROUTE
    ) {
        composable(Library.route) {
            LibraryScreen(
                navigateToSectionBooksScreen = { categoryName: String, type: String ->
                    navController.navigate(
                        SectionBooks.createRoute(
                            categoryName,
                            type
                        )
                    )
                },
                navigateToSearchResultsScreen = { categoryName: String, type: String ->
                    navController.navigate(
                        SearchResults.createRoute(
                            categoryName,
                            type
                        )
                    )
                }
            )
        }
        composable(
            Download.route,
        ) {
            DownloadScreen(
                navigateToSectionBooksScreen = { categoryName: String, type: String ->
                    navController.navigate(
                        SectionBooks.createRoute(
                            categoryName,
                            type
                        )
                    )
                },
                navigateToSearchResultsScreen = { categoryName: String, type: String ->
                    navController.navigate(
                        SearchResults.createRoute(
                            categoryName,
                            type
                        )
                    )
                }
            )
        }
        composable(Favorite.route) { FavoriteScreen() }
        composable(Search.route) { SearchScreen() }
        composable(Settings.route) { SettingsScreen() }

        composable(
            SectionBooks.route,
            arguments = listOf(
                navArgument("categoryName") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType },
            )
        ) {
            it.arguments?.getString("categoryName")?.let { categoryName ->
                SectionBooksScreen(
                    categoryName = categoryName,
                    navigateToSearchResultsScreen = { categoryName: String, type: String ->
                        navController.navigate(
                            SearchResults.createRoute(
                                categoryName,
                                type
                            )
                        )
                    },
                    navigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            SearchResults.route,
            arguments = listOf(
                navArgument("categoryName") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType },
            )
        ) {

            SearchResultsScreen(navigateToSectionBooksScreen = { categoryName: String, type: String ->
                navController.navigate(
                    SectionBooks.createRoute(
                        categoryName,
                        type
                    )
                ){
                    popUpTo(Download.route)
                }
            }) { navController.popBackStack() }
        }


    }
}
