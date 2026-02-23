package com.example.hydraping.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Sip")
    data object History : Screen("history", "States")
    data object Settings : Screen("settings", "Remind")
    data object CreateTarget : Screen("create_target", "New Target")
}
