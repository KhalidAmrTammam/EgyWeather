package com.iti.java.egyweather

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.iti.java.egyweather.Model.LocalDataSource.WeatherDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val alertId = intent?.getLongExtra("alertId", -1L) ?: return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(alertId.toInt())

        CoroutineScope(Dispatchers.IO).launch {
            WeatherDatabase.getInstance(context)
                .weatherDao()
                .updateAlertStatus(alertId, false)
        }
    }
}