package com.iti.java.egyweather.Model.BOJO

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromMain(main: Main): String = gson.toJson(main)

    @TypeConverter
    fun toMain(json: String): Main = gson.fromJson(json, Main::class.java)

    @TypeConverter
    fun fromWeatherList(weather: List<Weather>): String = gson.toJson(weather)

    @TypeConverter
    fun toWeatherList(json: String): List<Weather> =
        gson.fromJson(json, object : TypeToken<List<Weather>>() {}.type)

    @TypeConverter
    fun fromWind(wind: Wind): String = gson.toJson(wind)

    @TypeConverter
    fun toWind(json: String): Wind = gson.fromJson(json, Wind::class.java)

    @TypeConverter
    fun fromCoord(coord: Coord): String = gson.toJson(coord)

    @TypeConverter
    fun toCoord(json: String): Coord = gson.fromJson(json, Coord::class.java)

    @TypeConverter
    fun fromCloud(clouds: Clouds): String = gson.toJson(clouds)

    @TypeConverter
    fun toCloud(json: String): Clouds = gson.fromJson(json, Clouds::class.java)

    @TypeConverter
    fun fromSys(sys: Sys): String = gson.toJson(sys)

    @TypeConverter
    fun toSys(json: String): Sys = gson.fromJson(json, Sys::class.java)

    @TypeConverter
    fun fromRain(rain: Rain): String = gson.toJson(rain)

    @TypeConverter
    fun toRain(json: String): Rain = gson.fromJson(json, Rain::class.java)

    @TypeConverter
    fun fromCity(city: City): String = gson.toJson(city)

    @TypeConverter
    fun toCity(json: String): City = gson.fromJson(json, City::class.java)

    @TypeConverter
    fun fromForecastList(list: List<ForecastItem>): String = gson.toJson(list)

    @TypeConverter
    fun toForecastList(json: String): List<ForecastItem> =
        gson.fromJson(json, object : TypeToken<List<ForecastItem>>() {}.type)

    @TypeConverter
    fun fromForecastItem(item: ForecastItem): String = gson.toJson(item)

    @TypeConverter
    fun toForecastItem(json: String): ForecastItem = gson.fromJson(json, ForecastItem::class.java)
}
