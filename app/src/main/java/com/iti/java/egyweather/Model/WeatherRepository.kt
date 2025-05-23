package com.iti.java.egyweather.Model

import android.content.Context
import android.util.Log
import androidx.room.Transaction
import com.iti.java.egyweather.Model.BOJO.FavoriteLocation
import com.iti.java.egyweather.Model.BOJO.ForecastResponse
import com.iti.java.egyweather.Model.BOJO.WeatherAlert
import com.iti.java.egyweather.Model.BOJO.WeatherResponse
import com.iti.java.egyweather.Model.LocalDataSource.WeatherDao
import com.iti.java.egyweather.Model.RemoteDataSource.RemoteDataSource
import com.iti.java.egyweather.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException

class WeatherRepository(
    private val remoteDataSource: RemoteDataSource,
    private val weatherDao: WeatherDao,
    private val context: Context
) {
    private val TAG = "WeatherRepo"
    suspend fun getWeather(lat: String, lon: String, apiKey: String, forceRefresh: Boolean): Flow<WeatherResponse> = flow {
        val latDouble = lat.toDouble()
        val lonDouble = lon.toDouble()

        try {
            if (forceRefresh) {
                weatherDao.deleteWeatherByLatLon(latDouble, lonDouble)
                Log.d(TAG, "Forced cache clearance for weather data")
            }
            if (NetworkUtils.isInternetAvailable(context)) {
                Log.d(TAG, "Fetching fresh weather data from network")
                val result = remoteDataSource.fetchWeather(lat, lon, apiKey)
                if (result.isSuccess) {
                    val newData = result.getOrNull()!!.copy(
                        lat = latDouble,
                        lon = lonDouble
                    )
                    Log.d(TAG, "Network weather data received, updating cache")
                    weatherDao.deleteWeatherByLatLon(latDouble, lonDouble)
                    weatherDao.insert(newData)
                    emit(newData)
                }
            }else {
                Log.i(TAG, "Fetching cached weather data for lat=$lat, lon=$lon")
                weatherDao.getWeatherByLatLon(latDouble, lonDouble)?.let { emit(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather data: ${e.message}")
            val localData = weatherDao.getWeatherByLatLon(latDouble, lonDouble)
            if (localData == null) {
                Log.w(TAG, "No cached weather data available")
                throw IOException("No internet or cached data").apply { initCause(e) }
            }
            else emit(localData)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getForecast(lat: String, lon: String, apiKey: String,forceRefresh: Boolean): Flow<ForecastResponse> =
        flow {
            val latDouble = lat.toDouble()
            val lonDouble = lon.toDouble()
            try {
                if (forceRefresh) {
                    weatherDao.deleteForecastByLatLon(latDouble, lonDouble)
                    Log.d(TAG, "Forced cache clearance for weather data")
                }

                if (NetworkUtils.isInternetAvailable(context)) {

                    val result = remoteDataSource.fetchFutureWeather(lat, lon, apiKey)
                    if (result.isSuccess) {
                        val newData = result.getOrNull()!!.copy(
                            lat = latDouble,
                            lon = lonDouble
                        )
                        weatherDao.deleteForecastByLatLon(latDouble, lonDouble)
                        weatherDao.insertForecast(newData)
                        emit(newData)
                    }
                }else{
                    weatherDao.getForecastByLatLon(latDouble, lonDouble)?.let { emit(it) }
                }
            } catch (e: Exception) {
                val localData = weatherDao.getForecastByLatLon(latDouble, lonDouble)
                if (localData == null) throw IOException("No internet or cached data").apply {
                    initCause(
                        e
                    )
                }
                else emit(localData)
            }
        }.flowOn(Dispatchers.IO)

    @Transaction
    suspend fun syncWithRemote(lat: String, lon: String, apiKey: String) {
        if (!NetworkUtils.isInternetAvailable(context)) return

        try {
            remoteDataSource.fetchWeather(lat, lon, apiKey).getOrNull()?.let {
                weatherDao.run {
                    deleteWeatherByLatLon(lat.toDouble(), lon.toDouble())
                    insert(it)
                }
            }

            remoteDataSource.fetchFutureWeather(lat, lon, apiKey).getOrNull()?.let {
                weatherDao.run {
                    deleteForecastByLatLon(lat.toDouble(), lon.toDouble())
                    insertForecast(it)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
        }
    }

    suspend fun addFavorite(favorite: FavoriteLocation) {
        weatherDao.insertFavorite(favorite)
    }

    suspend fun removeFavorite(id: Long) {
        weatherDao.deleteFavorite(id)
    }

    fun getFavorites(): Flow<List<FavoriteLocation>> {
        return weatherDao.getFavorites()
    }
    suspend fun insertAlert(alert: WeatherAlert): Long {
        return weatherDao.insertAlert(alert)
    }

    suspend fun updateAlertStatus(alertId: Long, active: Boolean) {
        weatherDao.updateAlertStatus(alertId, active)
    }

    suspend fun deleteAlert(alertId: Long) {
        weatherDao.deleteAlert(alertId)
    }

    fun getAlerts(): Flow<List<WeatherAlert>> {
        return weatherDao.getAlerts()
    }
    suspend fun getAlertById(alertId: Long): WeatherAlert? {
        return weatherDao.getAlertById(alertId)
    }




}

