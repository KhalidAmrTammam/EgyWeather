package com.iti.java.egyweather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.iti.java.egyweather.Model.WeatherRepository

class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val workManager: WorkManager,
    private val settingsViewModel: SettingsViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(repository, workManager, settingsViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}