package com.iti.java.egyweather.Model.LocalDataSource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iti.java.egyweather.Model.BOJO.FavoriteLocation
import com.iti.java.egyweather.Model.BOJO.ForecastResponse
import com.iti.java.egyweather.Model.BOJO.WeatherResponse
import com.iti.java.egyweather.Model.BOJO.WeatherAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weather: WeatherResponse)

    @Query("SELECT * FROM weather_data WHERE cityId = :cityId")
    suspend fun getWeatherByCityId(cityId: Int): WeatherResponse?

    @Query("DELETE FROM weather_data WHERE cityId = :cityId")
    suspend fun deleteByCityId(cityId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: ForecastResponse)

    @Query("SELECT * FROM forecast_data WHERE cityId = :cityId")
    suspend fun getForecastByCityId(cityId: Int): ForecastResponse?

    @Query("DELETE FROM forecast_data WHERE cityId = :cityId")
    suspend fun deleteForecastByCityId(cityId: Int)

    @Query("SELECT * FROM weather_data WHERE lat = :lat AND lon = :lon")
    suspend fun getWeatherByLatLon(lat: Double, lon: Double): WeatherResponse?

    @Query("SELECT * FROM forecast_data WHERE lat = :lat AND lon = :lon")
    suspend fun getForecastByLatLon(lat: Double, lon: Double): ForecastResponse?

    @Query("DELETE FROM weather_data WHERE lat = :lat AND lon = :lon")
    suspend fun deleteWeatherByLatLon(lat: Double, lon: Double)

    @Query("DELETE FROM forecast_data WHERE lat = :lat AND lon = :lon")
    suspend fun deleteForecastByLatLon(lat: Double, lon: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteLocation)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavorite(id: Long)

    @Query("SELECT * FROM favorites ORDER BY created_At DESC")
    fun getFavorites(): Flow<List<FavoriteLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: WeatherAlert): Long

    @Query("UPDATE weather_alerts SET isActive = :active WHERE id = :alertId")
    suspend fun updateAlertStatus(alertId: Long, active: Boolean)

    @Query("DELETE FROM weather_alerts WHERE id = :alertId")
    suspend fun deleteAlert(alertId: Long)

    @Query("SELECT * FROM weather_alerts ORDER BY alertTime DESC")
    fun getAlerts(): Flow<List<WeatherAlert>>

    @Query("SELECT * FROM weather_alerts WHERE id = :alertId")
    suspend fun getAlertById(alertId: Long): WeatherAlert?
}
