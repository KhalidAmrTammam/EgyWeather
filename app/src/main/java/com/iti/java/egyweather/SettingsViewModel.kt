package com.iti.java.egyweather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    val uiState: StateFlow<SettingsState> = settingsRepository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun updateSettings(newSettings: SettingsState) {
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
        }
    }

    fun setLanguage(lang: String) {
        val newState = uiState.value.copy(
            language = lang,
            useSystemLanguage = (lang == "system")
        )
        updateSettings(newState)

        if (lang != "system") {
            Locale.setDefault(Locale(lang))
        }
    }

    fun updateLocationSource(useGPS: Boolean) {
        val source = if(useGPS) "gps" else "map"
        updateSettings(uiState.value.copy(locationSource = source))
    }

    fun updateTempUnit(unit: String) {
        updateSettings(uiState.value.copy(tempUnit = unit))
    }

    fun updateWindUnit(unit: String) {
        updateSettings(uiState.value.copy(windUnit = unit))
    }
}