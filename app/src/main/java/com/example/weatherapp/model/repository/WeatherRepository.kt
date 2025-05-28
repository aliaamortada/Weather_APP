package com.example.weatherapp.model.repository

import com.example.weatherapp.model.local.FavCityEntity
import com.example.weatherapp.model.local.WeatherEntity
import com.example.weatherapp.model.local.WeatherLocalDataSource
import com.example.weatherapp.model.pojo.ForecastResponse
import com.example.weatherapp.model.remote.WeatherRemoteDataSource
import kotlin.Result


interface WeatherRepository {
    // Favorite Cities (Local Operations)
    suspend fun getAllFavCities(): List<FavCityEntity>
    suspend fun insertFavCity(city: FavCityEntity)
    suspend fun deleteFavCity(city: FavCityEntity)
    suspend fun getCityById(cityId: Int): FavCityEntity?
    suspend fun getCityByName(name: String): FavCityEntity?

    // Weather (Remote Operations)
    suspend fun getForecast(lat: Double, lon: Double): Result<ForecastResponse>
    suspend fun getForecastByCityName(cityName: String): Result<ForecastResponse>
    suspend fun getForecastByCityId(cityId: Long): Result<ForecastResponse>

    // Weather (Local Operations)
    suspend fun getAllWeather(): List<WeatherEntity>
    suspend fun insertWeather(weather: WeatherEntity)
    suspend fun clearWeather()
    suspend fun getWeatherByTimestamp(timestamp: Long): WeatherEntity?
    suspend fun getForecastByDateAndCityId(dt: Long, cityId: Int): WeatherEntity?

    suspend fun getForecastsForCityInRange(
        cityId: Int,
        startDate: Long,
        endDate: Long
    ): List<WeatherEntity>

    suspend fun deleteForecastById(dt: Long)
    suspend fun fetchAndStoreForecastByCityName(cityName: String): Boolean
    suspend fun loadForecastFromDbByCityName(cityName: String): ForecastResponse?
}