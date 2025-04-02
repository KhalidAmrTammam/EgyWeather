package com.iti.java.egyweather

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.iti.java.egyweather.Model.LocalDataSource.WeatherDatabase
import com.iti.java.egyweather.Model.RemoteDataSource.RemoteDataSource
import com.iti.java.egyweather.Model.RemoteDataSource.RetrofitHelper
import com.iti.java.egyweather.Model.WeatherRepository

class WeatherSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val API_KEY = "2c979ca333a5bf78ab2d861816b89ec5"
    override suspend fun doWork(): Result {
        return try {
            val lat = inputData.getString("lat") ?: return Result.failure()
            val lon = inputData.getString("lon") ?: return Result.failure()

            val repository = WeatherRepository(
                RemoteDataSource(RetrofitHelper.api),
                WeatherDatabase.getInstance(applicationContext).weatherDao(),
                applicationContext
            )

            if (NetworkUtils.isInternetAvailable(applicationContext)) {
                repository.syncWithRemote(lat, lon,API_KEY)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}