package com.iti.java.egyweather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    val uiState: StateFlow<SettingsState> = settingsRepository.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsState()
        )

    fun updateSettings(newSettings: SettingsState) {
        settingsRepository.updateSettings(newSettings)
    }
}