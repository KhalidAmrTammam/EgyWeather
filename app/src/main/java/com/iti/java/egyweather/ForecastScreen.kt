package com.iti.java.egyweather

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ForecastScreen(
    lat: Double,
    lon: Double,
    viewModel: WeatherViewModel // No navController here
) {
    LaunchedEffect(lat, lon) {
        viewModel.loadWeather(lat.toString(), lon.toString())
    }
    WeatherScreen(viewModel = viewModel)
}
