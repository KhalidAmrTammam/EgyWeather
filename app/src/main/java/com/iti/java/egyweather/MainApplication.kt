package com.iti.java.egyweather

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.iti.java.egyweather.Model.LocalDataSource.WeatherDatabase
import com.iti.java.egyweather.Model.RemoteDataSource.RemoteDataSource
import com.iti.java.egyweather.Model.RemoteDataSource.RetrofitHelper
import com.iti.java.egyweather.Model.WeatherRepository
import java.util.Locale

class MainApplication : Application() {
    lateinit var placesClient: PlacesClient
    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyD3yetxMtpwjWOXNbDcSX8-G6mMl0v0lV4")
        }
        placesClient = Places.createClient(this)
        updateLanguage()
    }


    val repository: WeatherRepository by lazy {
        WeatherRepository(
            RemoteDataSource(RetrofitHelper.api),
            WeatherDatabase.getInstance(this).weatherDao(),
            this
        )
    }

    private fun updateLanguage() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val useSystem = prefs.getBoolean("useSystemLanguage", true)
        val lang = if(useSystem) "system" else prefs.getString("language", "en") ?: "en"

        val locale = when {
            useSystem -> Locale.getDefault()
            lang == "ar" -> Locale("ar")
            else -> Locale("en")
        }

        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}