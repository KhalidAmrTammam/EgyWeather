package com.iti.java.egyweather

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class AlertSnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val alertId = intent?.getLongExtra("alertId", -1L) ?: return
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)

        val workRequest = OneTimeWorkRequestBuilder<WeatherAlertWorker>()
            .setInputData(workDataOf(
                "lat" to lat,
                "lon" to lon,
                "alertId" to alertId
            ))
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}