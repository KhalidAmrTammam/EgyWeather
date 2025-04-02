package com.iti.java.egyweather.Model.RemoteDataSource

import android.util.Log
import com.iti.java.egyweather.Model.BOJO.ForecastResponse
import com.iti.java.egyweather.Model.BOJO.WeatherResponse
import kotlinx.coroutines.delay
import okio.IOException
import retrofit2.HttpException

class RemoteDataSource(private val api: WeatherApi) {
    private val RETRY_COUNT = 3
    private val INITIAL_RETRY_DELAY = 1000L

    suspend fun fetchWeather(lat: String, lon: String, apiKey: String): Result<WeatherResponse> {
        return executeWithRetry { api.getWeather(lat, lon, apiKey) }
    }

    suspend fun fetchFutureWeather(lat: String, lon: String, apiKey: String): Result<ForecastResponse> {
        return executeWithRetry { api.getForecast(lat, lon, apiKey) }
    }

    private suspend fun <T> executeWithRetry(
        block: suspend () -> T
    ): Result<T> {
        var currentDelay = INITIAL_RETRY_DELAY
        repeat(RETRY_COUNT) { attempt ->
            try {
                return Result.success(block())
            } catch (e: IOException) {
                Log.e("RemoteDataSource", "Network error (attempt ${attempt + 1}/$RETRY_COUNT)", e)
                if (attempt == RETRY_COUNT - 1) return Result.failure(e)
            } catch (e: HttpException) {
                Log.e("RemoteDataSource", "HTTP error ${e.code()}", e)
                return Result.failure(e)
            }
            delay(currentDelay)
            currentDelay *= 2
        }
        return Result.failure(IOException("Failed after $RETRY_COUNT attempts"))
    }
}