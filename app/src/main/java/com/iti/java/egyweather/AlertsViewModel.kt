package com.iti.java.egyweather

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.iti.java.egyweather.Model.BOJO.WeatherAlert
import com.iti.java.egyweather.Model.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AlertsViewModel(
    private val repository: WeatherRepository,
    private val application: Application
) : ViewModel() {

    val alerts = repository.getAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlert(lat: Double, lon: Double, time: Long, type: String, duration: Int) {
        viewModelScope.launch {
            try {
                if (time < System.currentTimeMillis()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(application, "Cannot schedule alerts in past", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val locationName = withContext(Dispatchers.IO) {
                    try {
                        Geocoder(application, Locale.getDefault()).run {
                            if (Build.VERSION.SDK_INT >= 33) {
                                getFromLocation(lat, lon, 1)?.firstOrNull()?.getAddressLine(0)
                            } else {
                                getFromLocation(lat, lon, 1)?.firstOrNull()?.getAddressLine(0)
                            }
                        } ?: "Unknown Location"
                    } catch (e: Exception) {
                        "Selected Location"
                    }
                }

                val alert = WeatherAlert(
                    alertTime = time,
                    lat = lat,
                    lon = lon,
                    locationName = locationName,
                    alertType = type,
                    durationMinutes = duration
                )

                val alertId = repository.insertAlert(alert)
                scheduleAlertWorker(alertId, lat, lon, time)

            } catch (e: Exception) {
                Log.e("AlertsViewModel", "Error adding alert", e)
            }
        }
    }

    private fun scheduleAlertWorker(alertId: Long, lat: Double, lon: Double, time: Long) {
        val workRequest = OneTimeWorkRequestBuilder<WeatherAlertWorker>()
            .setInputData(workDataOf(
                "lat" to lat,
                "lon" to lon,
                "alertId" to alertId
            ))
            .setInitialDelay(maxOf(0, time - System.currentTimeMillis()), TimeUnit.MILLISECONDS)
            .addTag("WEATHER_ALERT")
            .build()

        WorkManager.getInstance(application).enqueue(workRequest)
    }

    fun toggleAlert(alertId: Long, active: Boolean) {
        viewModelScope.launch {
            repository.updateAlertStatus(alertId, active)
        }
    }

    fun deleteAlert(alertId: Long) {
        viewModelScope.launch {
            repository.deleteAlert(alertId)
            WorkManager.getInstance(application)
                .cancelAllWorkByTag("alert_$alertId")
        }
    }
}