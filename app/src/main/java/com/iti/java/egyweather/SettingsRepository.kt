package com.iti.java.egyweather

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsRepository {
    private val _userSettings = MutableStateFlow(SettingsState())
    val userSettings: StateFlow<SettingsState> = _userSettings

    fun updateSettings(newSettings: SettingsState) {
        _userSettings.value = newSettings
    }
}