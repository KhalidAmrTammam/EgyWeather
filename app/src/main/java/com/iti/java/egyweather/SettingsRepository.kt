package com.iti.java.egyweather

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _userSettings = MutableStateFlow(loadSettings())
    val userSettings: StateFlow<SettingsState> = _userSettings

    fun updateSettings(newSettings: SettingsState) {
        _userSettings.value = newSettings
        saveSettings(newSettings)
    }

    private fun loadSettings(): SettingsState = SettingsState(
        tempUnit = prefs.getString("tempUnit", "celsius") ?: "celsius",
        windUnit = prefs.getString("windUnit", "m/s") ?: "m/s",
        locationSource = prefs.getString("locationSource", "gps") ?: "gps",
        language = prefs.getString("language", "system") ?: "system",
        useSystemLanguage = prefs.getBoolean("useSystemLanguage", true),
        manualLat = prefs.getFloat("manualLat", 0f).toDouble().takeIf { it != 0.0 },
        manualLon = prefs.getFloat("manualLon", 0f).toDouble().takeIf { it != 0.0 }
    )

    private fun saveSettings(settings: SettingsState) = with(prefs.edit()) {
        putString("tempUnit", settings.tempUnit)
        putString("windUnit", settings.windUnit)
        putString("locationSource", settings.locationSource)
        putString("language", settings.language)
        putBoolean("useSystemLanguage", settings.useSystemLanguage)
        putFloat("manualLat", settings.manualLat?.toFloat() ?: 0f)
        putFloat("manualLon", settings.manualLon?.toFloat() ?: 0f)
        apply()
    }
}