package com.iti.java.egyweather.Model.BOJO


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName

@Entity(tableName = "forecast_data", primaryKeys = ["lat", "lon"])
@TypeConverters(WeatherTypeConverters::class)
data class ForecastResponse(
    val cityId: Int,
    val city: City,
    val list: List<ForecastItem>,
    @ColumnInfo(name = "lat") val lat: Double = city.coord.lat,
    @ColumnInfo(name = "lon") val lon: Double = city.coord.lon
)

data class City(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("coord") val coord: Coord,
    @SerializedName("country") val country: String,
    @SerializedName("timezone") val timezone: Int
)

data class ForecastItem(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("clouds") val clouds: Clouds,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("dt_txt") val dtTxt: String
)