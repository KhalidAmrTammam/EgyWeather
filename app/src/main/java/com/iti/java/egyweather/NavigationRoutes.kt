package com.iti.java.egyweather

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Favorites : Screen("favorites")
    object Alerts : Screen("alerts")
    object Settings : Screen("settings")
}