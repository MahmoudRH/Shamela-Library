package com.shamela.library.presentaion.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.shamela.library.presentaion.common.DefaultTopBar
import com.shamela.library.presentaion.navigation.HomeHostDestination
import com.shamela.library.presentaion.navigation.NavigationGraphs
import com.shamela.library.presentaion.navigation.homeGraph
import com.shamela.library.presentaion.theme.AppFonts
import com.shamela.library.presentaion.utils.Utils

private val destination = listOf(
    HomeHostDestination.Library,
    HomeHostDestination.Download,
    HomeHostDestination.Favorite,
    HomeHostDestination.Search,
    HomeHostDestination.Settings,
)

@Composable
fun HomeHostScreen() {
    val navController = rememberNavController()
    val bottomBarVisibility = remember { mutableStateOf(false) }
    bottomBarVisibility.value =
        (Utils.parentGraphRoute(navController) == NavigationGraphs.HOME_GRAPH_ROUTE)
    var selectedScreenTitle by remember {
        mutableStateOf(destination[0].label)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (bottomBarVisibility.value) {
                NavigationBar() {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    destination.forEach { screen ->
                        val isSelected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        if (isSelected){ selectedScreenTitle = screen.label}

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
            DefaultTopBar(selectedScreenTitle)
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