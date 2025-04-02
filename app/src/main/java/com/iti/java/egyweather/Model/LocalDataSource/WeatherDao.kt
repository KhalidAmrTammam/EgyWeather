package com.iti.java.egyweather.Model.LocalDataSource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iti.java.egyweather.Model.BOJO.ForecastResponse
import com.iti.java.egyweather.Model.BOJO.WeatherResponse

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
}
