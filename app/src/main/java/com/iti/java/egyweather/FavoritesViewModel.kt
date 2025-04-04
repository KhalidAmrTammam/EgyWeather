package com.iti.java.egyweather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.java.egyweather.Model.BOJO.FavoriteLocation
import com.iti.java.egyweather.Model.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class FavoritesViewModel(private val repository: WeatherRepository) : ViewModel() {
    val favorites: StateFlow<List<FavoriteLocation>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFavorite(location: FavoriteLocation) {
        viewModelScope.launch {
            repository.addFavorite(location)
        }
    }

    fun removeFavorite(id: Long) {
        viewModelScope.launch {
            repository.removeFavorite(id)
        }
    }
}