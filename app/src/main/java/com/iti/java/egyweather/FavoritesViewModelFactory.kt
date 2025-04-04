package com.iti.java.egyweather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.java.egyweather.Model.WeatherRepository

class FavoritesViewModelFactory(
    private val repository: WeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavoritesViewModel(repository) as T
    }
}