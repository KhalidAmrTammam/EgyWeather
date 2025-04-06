package com.iti.java.egyweather

data class SettingsState(
    val tempUnit: String = "celsius",
    val windUnit: String = "m/s",
    val locationSource: String = "gps",
    val language: String = "system",
    val useSystemLanguage: Boolean = true,
    val manualLat: Double? = null,
    val manualLon: Double? = null
)