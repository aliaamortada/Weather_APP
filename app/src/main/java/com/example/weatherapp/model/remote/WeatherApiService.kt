package com.example.weatherapp.model.remote

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.model.pojo.ForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/forecast")
    suspend fun getForecastByLatLon(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en",
        @Query("appid") appId: String = BuildConfig.OPEN_WEATHER_API_KEY
    ): ForecastResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastByCityName(
        @Query("q") cityName: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en",
        @Query("appid") appId: String= BuildConfig.OPEN_WEATHER_API_KEY

    ): ForecastResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastByCityId(
        @Query("id") cityId: Long,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en",
        @Query("appid") appId: String = BuildConfig.OPEN_WEATHER_API_KEY
    ): ForecastResponse

}