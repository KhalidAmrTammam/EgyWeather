package com.iti.java.egyweather

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.iti.java.egyweather.Model.LocalDataSource.WeatherDatabase
import com.iti.java.egyweather.Model.RemoteDataSource.RemoteDataSource
import com.iti.java.egyweather.Model.RemoteDataSource.RetrofitHelper
import com.iti.java.egyweather.Model.WeatherRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class WeatherAlertWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
        private val API_KEY = "2c979ca333a5bf78ab2d861816b89ec5"
        private lateinit var repository: WeatherRepository


    override suspend fun doWork(): Result {
        return try {
            repository = WeatherRepository(
                RemoteDataSource(RetrofitHelper.api),
                WeatherDatabase.getInstance(applicationContext).weatherDao(),
                applicationContext
            )

            val lat = inputData.getDouble("lat", 0.0)
            val lon = inputData.getDouble("lon", 0.0)
            val alertId = inputData.getLong("alertId", -1L)

            if (NetworkUtils.isInternetAvailable(applicationContext)) {
                repository.syncWithRemote(lat.toString(), lon.toString(), API_KEY)
            }

            showNotification(applicationContext, alertId, lat, lon)
            repository.updateAlertStatus(alertId, false)

            Result.success()
        } catch (e: Exception) {
            Log.e("WeatherAlertWorker", "Alert failed", e)
            Result.retry()
        }
    }

    private suspend fun showNotification(context: Context, alertId: Long, lat: Double, lon: Double) {
        try {
            val weather = repository.getWeather(lat.toString(), lon.toString(), API_KEY, false).firstOrNull()
            val alert = repository.getAlertById(alertId)

            if (weather == null || alert == null || alert.locationName.isBlank()) return

            val intent = Intent(context, WeatherDetailActivity::class.java).apply {
                putExtra("lat", lat)
                putExtra("lon", lon)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                alertId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val snoozeIntent = Intent(context, AlertSnoozeReceiver::class.java).apply {
                putExtra("alertId", alertId)
                putExtra("lat", lat)
                putExtra("lon", lon)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                Random.nextInt(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, "weather_alerts_channel")
                .setContentTitle("Weather Alert - ${alert.locationName}")
                .setContentText("Current temp: ${weather.main.temp}°C")
                .setSmallIcon(R.drawable.ic_weather_alert)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(
                    R.drawable.ic_snooze,
                    "Snooze",
                    snoozePendingIntent
                )
                .addAction(
                    R.drawable.ic_close,
                    "Dismiss",
                    NotificationUtils.getDismissPendingIntent(context, alertId)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            if (NotificationUtils.hasNotificationPermission(context)) {
                NotificationManagerCompat.from(context)
                    .notify(alertId.toInt(), notification.build())
            }
        } catch (e: Exception) {
            Log.e("WeatherAlertWorker", "Notification failed", e)
        }
    }


}