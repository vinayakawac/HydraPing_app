package com.example.hydraping.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Home")
    data object History : Screen("history", "History")
    data object Settings : Screen("settings", "Settings")
}
