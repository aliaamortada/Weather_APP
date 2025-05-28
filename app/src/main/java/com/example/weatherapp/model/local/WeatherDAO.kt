package com.example.weatherapp.model.local

import androidx.room.*

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather_forecast")
    suspend fun clearWeather()

    @Query("SELECT * FROM weather_forecast ORDER BY dt ASC")
    suspend fun getAllWeather(): List<WeatherEntity>

    @Query("SELECT * FROM weather_forecast WHERE dt = :timestamp LIMIT 1")
    suspend fun getWeatherByTimestamp(timestamp: Long): WeatherEntity?

    @Query("SELECT * FROM weather_forecast WHERE dt = :dt AND cityId = :cityId")
    suspend fun getForecastByDateAndCityId(dt: Long, cityId: Int): WeatherEntity?

    @Query("SELECT * FROM weather_forecast WHERE cityId = :cityId AND dt BETWEEN :startDate AND :endDate")
    suspend fun getForecastsForCityInRange(
        cityId: Int,
        startDate: Long,
        endDate: Long
    ): List<WeatherEntity>

    @Query("DELETE FROM weather_forecast WHERE dt < :dt")
    suspend fun deleteForecastById(dt: Long)
}

