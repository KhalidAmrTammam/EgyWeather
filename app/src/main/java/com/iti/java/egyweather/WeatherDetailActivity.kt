package com.iti.java.egyweather

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkManager
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.iti.java.egyweather.Model.LocalDataSource.WeatherDatabase
import com.iti.java.egyweather.Model.RemoteDataSource.RemoteDataSource
import com.iti.java.egyweather.Model.RemoteDataSource.RetrofitHelper
import com.iti.java.egyweather.Model.WeatherRepository
import java.util.Locale

class WeatherDetailActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)

        setContent {
            val viewModel: WeatherViewModel = viewModel(
                factory = WeatherViewModelFactory(
                    (application as MainApplication).repository,
                    WorkManager.getInstance(applicationContext),
                    settingsViewModel
                )
            )

            ForecastScreen(lat = lat, lon = lon, viewModel = viewModel)
        }
    }
}

