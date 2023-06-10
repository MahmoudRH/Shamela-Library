package com.shamela.library.presentaion.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    @Composable
    fun parentGraphRoute(navController: NavHostController): String? {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        return navBackStackEntry?.destination?.parent?.route
    }

    @Composable
    fun currentRoute(navController: NavHostController): String? {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        return navBackStackEntry?.destination?.route
    }

    fun formatDate(timestamp:Long): String {
        val date = Date(timestamp)
        return SimpleDateFormat("dd MMM ", Locale.getDefault()).format(date)
    }
    fun formatDate(timestamp:String): String {
        if (timestamp.isEmpty()) return ""
        val date = Date(timestamp.toLong())
        return SimpleDateFormat("dd-MM-y", Locale.getDefault()).format(date)
    }
}