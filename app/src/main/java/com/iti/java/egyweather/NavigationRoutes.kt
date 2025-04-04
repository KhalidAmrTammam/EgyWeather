package com.iti.java.egyweather

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Favorites : Screen("favorites")
    object Alerts : Screen("alerts")
    object Settings : Screen("settings")
    data class Forecast(val lat: Double, val lon: Double) :
        Screen("forecast/{lat}/{lon}") {
        companion object {
            val route = "forecast/{lat}/{lon}"
        }
    }
}
