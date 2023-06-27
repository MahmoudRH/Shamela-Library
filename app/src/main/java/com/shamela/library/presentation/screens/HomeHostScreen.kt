package com.shamela.library.presentation.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shamela.apptheme.common.DefaultTopBar
import com.shamela.apptheme.theme.AppFonts
import com.shamela.library.presentation.navigation.Download
import com.shamela.library.presentation.navigation.Favorite
import com.shamela.library.presentation.navigation.Library
import com.shamela.library.presentation.navigation.NavigationGraphs
import com.shamela.library.presentation.navigation.Search
import com.shamela.library.presentation.navigation.SearchResults
import com.shamela.library.presentation.navigation.SectionBooks
import com.shamela.library.presentation.navigation.Settings
import com.shamela.library.presentation.navigation.homeGraph
import com.shamela.library.presentation.utils.NavigationUtils

private val destination = listOf(
    Library,
    Download,
    Favorite,
    Search,
    Settings,
)

@Composable
fun HomeHostScreen() {
    val navController = rememberNavController()
    val screenBarsVisibility = remember { mutableStateOf(true) }
    screenBarsVisibility.value =
        if (NavigationUtils.parentGraphRoute(navController) == NavigationGraphs.HOME_GRAPH_ROUTE) {
            val currentRoute =  NavigationUtils.currentRoute(navController)
            val categoryName = navController.currentBackStackEntry?.arguments?.getString("categoryName")
            Log.e("Mah", "HomeHostScreen: currentRout = $currentRoute")
            Log.e("Mah", "HomeHostScreen: categoryName = $categoryName")
            if (currentRoute == SearchResults.route){
                categoryName == "all"
            }else{
                currentRoute != SectionBooks.route
            }
        }
        else
            false

    var selectedScreen by remember { mutableIntStateOf(0) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = screenBarsVisibility.value,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                NavigationBar() {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    destination.forEachIndexed { index, screen ->
                        val isSelected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        if (isSelected) {
                            selectedScreen = index
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {

                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unSelectedIcon,
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(
                                    text = screen.label,
                                    style = AppFonts.textSmallBold.copy(fontSize = 14.sp),
                                )
                            },
                            alwaysShowLabel = false
                        )
                    }
                }
            }
        },
        topBar = {
            AnimatedVisibility(
                visible = screenBarsVisibility.value,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
            ) {
                DefaultTopBar(
                    title = destination[selectedScreen].label,
                    actionIcon = destination[selectedScreen].actionIcon,
                    onActionClick = { destination[selectedScreen].onActionClick() }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = NavigationGraphs.HOME_GRAPH_ROUTE,
            modifier = Modifier.padding(it)
        ) {
            homeGraph(navController)
        }
    }
}