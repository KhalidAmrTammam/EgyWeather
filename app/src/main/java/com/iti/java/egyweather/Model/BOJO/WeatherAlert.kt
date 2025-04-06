package com.iti.java.egyweather.Model.BOJO

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "weather_alerts")
data class WeatherAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alertTime: Long,
    val lat: Double,
    val lon: Double,
    val locationName: String,
    val alertType: String,
    val durationMinutes: Int,
    val isActive: Boolean = true
) {
    fun isFutureAlert(): Boolean {
        return alertTime > Calendar.getInstance().timeInMillis
    }
}