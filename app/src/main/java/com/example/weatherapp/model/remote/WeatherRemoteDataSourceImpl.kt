package com.example.weatherapp.model.remote

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.model.pojo.ForecastResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class WeatherRemoteDataSourceImpl(private val weatherApiService: WeatherApiService) : WeatherRemoteDataSource {
    override suspend fun getForecast(lat: Double, lon: Double): Result<ForecastResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApiService.getForecastByLatLon(lat, lon, BuildConfig.OPEN_WEATHER_API_KEY)
                if (response.cod == "200") {
                    Result.success(response)
                } else {
                    Result.failure(Exception("API error: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override  suspend fun getForecastByCityName(cityName: String): Result<ForecastResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApiService.getForecastByCityName(cityName)

                if (response.cod == "200") {
                    Result.success(response)
                } else {
                    Result.failure(Exception("API error: ${response.message}"))
                }
            }

             catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override  suspend fun getForecastByCityId(cityId: Long): Result<ForecastResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApiService.getForecastByCityId(cityId, BuildConfig.OPEN_WEATHER_API_KEY)
                if (response.cod == "200") {
                    Result.success(response)
                } else {
                    Result.failure(Exception("API error: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

}