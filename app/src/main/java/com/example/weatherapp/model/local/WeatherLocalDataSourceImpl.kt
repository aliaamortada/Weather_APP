package com.example.weatherapp.model.local

class WeatherLocalDataSourceImpl(
    private val favCityDao: FavCityDao,
    private val weatherDao: WeatherDao
) : WeatherLocalDataSource {

    override suspend fun insertFavCity(city: FavCityEntity) {
        favCityDao.insertFavCity(city)
    }

    override suspend fun deleteFavCity(city: FavCityEntity) {
        favCityDao.deleteFavCity(city)
    }

    override suspend fun getAllFavCities(): List<FavCityEntity> {
        return favCityDao.getAllFavCities()
    }

    override suspend fun getCityById(cityId: Int): FavCityEntity? {
        return favCityDao.getCityById(cityId)
    }

    override suspend fun getCityByName(name: String): FavCityEntity? {
        return favCityDao.getCityByName(name)
    }

    override suspend fun insertWeather(weather: WeatherEntity) {
        weatherDao.insertWeather(weather)
    }

    override suspend fun clearWeather() {
        weatherDao.clearWeather()
    }

    override suspend fun getAllWeather(): List<WeatherEntity> {
        return weatherDao.getAllWeather()
    }

    override suspend fun getWeatherByTimestamp(timestamp: Long): WeatherEntity? {
        return weatherDao.getWeatherByTimestamp(timestamp)
    }

    override suspend fun getForecastByDateAndCityId(dt: Long, cityId: Int): WeatherEntity? {
        return weatherDao.getForecastByDateAndCityId(dt,cityId)
    }

    override suspend fun getForecastsForCityInRange(
        cityId: Int,
        startDate: Long,
        endDate: Long
    ): List<WeatherEntity> {
        return weatherDao.getForecastsForCityInRange(cityId, startDate, endDate)
    }

    override suspend fun deleteForecastById(dt: Long) {
        return weatherDao.deleteForecastById(dt)
    }

}
