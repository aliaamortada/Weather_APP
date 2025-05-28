package com.example.weatherapp.model.remote

import com.example.weatherapp.model.local.FavCityEntity
import com.example.weatherapp.model.local.WeatherEntity
import com.example.weatherapp.model.pojo.ForecastResponse


interface WeatherRemoteDataSource {
    suspend fun getForecast(lat: Double, lon: Double): Result<ForecastResponse>
    suspend fun getForecastByCityName(cityName: String): Result<ForecastResponse>
    suspend fun getForecastByCityId(cityId: Long): Result<ForecastResponse>
}

