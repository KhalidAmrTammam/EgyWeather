package com.iti.java.egyweather.Model.BOJO


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName


@Entity(tableName = "weather_data", primaryKeys = ["lat", "lon"])
@TypeConverters(WeatherTypeConverters::class)
data class WeatherResponse(
    @SerializedName("id") val cityId: Int,
    @SerializedName("base") val base: String,
    @SerializedName("name") val name: String,
    @SerializedName("main") val main: Main,
    @SerializedName("cod") val cod: Int,
    @SerializedName("coord") val coord: Coord,
    @ColumnInfo(name = "lat") val lat: Double = coord.lat ,
    @ColumnInfo(name = "lon") val lon: Double = coord.lon ,
    @SerializedName("clouds") val clouds: Clouds,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("sys") val sys: Sys,
    @SerializedName("rain") val rain: Rain? = null,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("dt") val dt: Long,
    @SerializedName("visibility") val visibility: Int
)

data class Main(
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("grnd_level") val grndLevel: Int? = null,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("sea_level") val seaLevel: Int? = null,
    @SerializedName("temp") val temp: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("temp_min") val tempMin: Double
)

data class Weather(
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String
)

data class Clouds(
    @SerializedName("all") val all: Int
)

data class Coord(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)

data class Rain(
    @SerializedName("1h") val oneHour: Double
)

data class Sys(
    @SerializedName("country") val country: String,
    @SerializedName("id") val id: Int,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long,
    @SerializedName("type") val type: Int
)

data class Wind(
    @SerializedName("deg") val deg: Int,
    @SerializedName("gust") val gust: Double? = null,
    @SerializedName("speed") val speed: Double
)
