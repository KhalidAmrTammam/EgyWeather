package com.iti.java.egyweather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.java.egyweather.Model.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow


class FavoritesViewModel(private val repository: WeatherRepository) : ViewModel() {
    val favorites: StateFlow<List<String>> = repository.getFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}