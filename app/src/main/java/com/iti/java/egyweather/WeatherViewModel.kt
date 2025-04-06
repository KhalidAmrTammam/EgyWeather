package com.iti.java.egyweather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.iti.java.egyweather.Model.BOJO.ForecastItem
import com.iti.java.egyweather.Model.BOJO.ForecastResponse
import com.iti.java.egyweather.Model.BOJO.WeatherResponse
import com.iti.java.egyweather.Model.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val workManager: WorkManager,
    private val settingsViewModel: SettingsViewModel
) : ViewModel() {
    private val _state = MutableStateFlow(WeatherState())
    val state: StateFlow<WeatherState> = _state

    private val API_KEY = "2c979ca333a5bf78ab2d861816b89ec5"

    fun loadWeather(lat: String, lon: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                repository.getWeather(lat, lon, API_KEY, forceRefresh)
                    .combine(
                        repository.getForecast(
                            lat,
                            lon,
                            API_KEY,
                            forceRefresh
                        )
                    ) { weather, forecast ->
                        val settingsState = settingsViewModel.uiState.value
                        _state.update {
                            it.copy(
                                weatherData = applyUnitConversions(weather, settingsState),
                                forecastData = forecast?.let { applyForecastUnits(it, settingsState) },
                                isLoading = false,
                            )
                        }
                    }.collect {}
            } catch (e: Exception) {
                val hasCachedData =
                    state.value.weatherData != null || state.value.forecastData != null
                val errorMessage = when (e) {
                    is IOException -> "Network error. ${if (!hasCachedData) "No cached data available." else "Showing cached data."}"
                    is HttpException -> "Server error: ${e.code()}"
                    else -> "Error: ${e.localizedMessage ?: "Unknown error"}"
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (!hasCachedData) errorMessage else null
                    )
                }
            }
        }
    }

    private fun applyUnitConversions(
        weather: WeatherResponse,
        settings: SettingsState
    ): WeatherResponse {
        return weather.copy(
            main = weather.main.copy(
                temp = convertTemperature(weather.main.temp, settings),
                tempMin = convertTemperature(weather.main.tempMin, settings),
                tempMax = convertTemperature(weather.main.tempMax, settings)
            ),
            wind = weather.wind.copy(
                speed = convertWindSpeed(weather.wind.speed, settings)
            )
        )
    }

    private fun applyForecastUnits(
        forecast: ForecastResponse,
        settings: SettingsState
    ): ForecastResponse {
        return forecast.copy(
            list = forecast.list.map { item ->
                item.copy(
                    main = item.main.copy(
                        temp = convertTemperature(item.main.temp, settings),
                        tempMin = convertTemperature(item.main.tempMin, settings),
                        tempMax = convertTemperature(item.main.tempMax, settings)
                    ),
                    wind = item.wind.copy(
                        speed = convertWindSpeed(item.wind.speed, settings)
                    )
                )
            }
        )
    }

    fun selectHourlyItem(item: ForecastItem) {
        _state.update {
            it.copy(selectedItems = it.selectedItems.copy(hourly = item))
        }
    }

    fun showDailyDetail(forecastItems: List<ForecastItem>) {
        _state.update {
            it.copy(selectedItems = it.selectedItems.copy(dailyDetail = forecastItems))
        }
    }

    fun clearSelection() {
        _state.update {
            it.copy(selectedItems = it.selectedItems.copy(hourly = null))
        }
    }

    fun clearDailyDetail() {
        _state.update {
            it.copy(selectedItems = it.selectedItems.copy(dailyDetail = null))
        }
    }

    fun scheduleWeatherSync(lat: String, lon: String) {
        val workRequest = PeriodicWorkRequestBuilder<WeatherSyncWorker>(
            3, TimeUnit.HOURS
        ).setInputData(
            workDataOf(
                "lat" to lat,
                "lon" to lon
            )
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "WeatherSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun convertTemperature(temp: Double, settings: SettingsState): Double {
        return UnitConverter.convertTemperature(
            temp,
            fromUnit = "celsius",
            toUnit = settings.tempUnit
        )
    }

    private fun convertWindSpeed(speed: Double, settings: SettingsState): Double {
        return UnitConverter.convertWindSpeed(
            speed,
            fromUnit = "m/s",
            toUnit = settings.windUnit
        )
    }
}

data class WeatherState(
    val weatherData: WeatherResponse? = null,
    val forecastData: ForecastResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedItems: SelectedItems = SelectedItems()
) {
    data class SelectedItems(
        val hourly: ForecastItem? = null,
        val daily: List<ForecastItem>? = null,
        val dailyDetail: List<ForecastItem>? = null
    )
}