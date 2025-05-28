package com.example.weatherapp.model.local

interface WeatherLocalDataSource {

    suspend fun insertFavCity(city: FavCityEntity)
    suspend fun deleteFavCity(city: FavCityEntity)
    suspend fun getAllFavCities(): List<FavCityEntity>
    suspend fun getCityById(cityId: Int): FavCityEntity?
    suspend fun getCityByName(name: String): FavCityEntity?

    suspend fun insertWeather(weather: WeatherEntity)
    suspend fun clearWeather()
    suspend fun getAllWeather(): List<WeatherEntity>
    suspend fun getWeatherByTimestamp(timestamp: Long): WeatherEntity?
    suspend fun getForecastByDateAndCityId(dt: Long, cityId: Int): WeatherEntity?

    suspend fun getForecastsForCityInRange(
        cityId: Int,
        startDate: Long,
        endDate: Long
    ): List<WeatherEntity>

    suspend fun deleteForecastById(dt: Long)
}
