package com.example.hydraping.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Sip")
    data object Remind : Screen("remind", "Remind")
    data object History : Screen("history", "States")
    data object Settings : Screen("settings", "Settings")
    data object CreateTarget : Screen("create_target", "New Target")
    data object EditTarget : Screen("edit_target/{targetId}", "Edit Target") {
        fun withId(targetId: Int) = "edit_target/$targetId"
    }
}
