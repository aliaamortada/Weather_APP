package com.example.weatherapp.model.repository

import android.util.Log
import com.example.weatherapp.model.local.FavCityEntity
import com.example.weatherapp.model.local.WeatherEntity
import com.example.weatherapp.model.local.WeatherLocalDataSource
import com.example.weatherapp.model.pojo.ForecastResponse
import com.example.weatherapp.model.pojo.Weather
import com.example.weatherapp.model.remote.RetrofitClient.weatherApiService
import com.example.weatherapp.model.remote.WeatherRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar


class WeatherRepositoryImpl(
    private val localDataSource: WeatherLocalDataSource,
    private val remoteDataSource: WeatherRemoteDataSource
) : WeatherRepository {

    // Favorite Cities (Local Operations)
    override suspend fun getAllFavCities(): List<FavCityEntity> =
        localDataSource.getAllFavCities()

    override suspend fun insertFavCity(city: FavCityEntity) =
        localDataSource.insertFavCity(city)

    override suspend fun deleteFavCity(city: FavCityEntity) =
        localDataSource.deleteFavCity(city)

    override suspend fun getCityById(cityId: Int): FavCityEntity? =
        localDataSource.getCityById(cityId)

    override suspend fun getCityByName(name: String): FavCityEntity? =
        localDataSource.getCityByName(name)


    // Weather (Remote Operations)
    override suspend fun getForecast(lat: Double, lon: Double): Result<ForecastResponse> =
        remoteDataSource.getForecast(lat, lon)

    override suspend fun getForecastByCityName(cityName: String): Result<ForecastResponse> {
        return remoteDataSource.getForecastByCityName(cityName)
    }


    override suspend fun getForecastByCityId(cityId: Long): Result<ForecastResponse> =
        remoteDataSource.getForecastByCityId(cityId)

    // Weather (Local Operations)
    override suspend fun getAllWeather(): List<WeatherEntity> =
        localDataSource.getAllWeather()

    override suspend fun insertWeather(weather: WeatherEntity) =
        localDataSource.insertWeather(weather)

    override suspend fun clearWeather() =
        localDataSource.clearWeather()

    override suspend fun getWeatherByTimestamp(timestamp: Long): WeatherEntity? =
        localDataSource.getWeatherByTimestamp(timestamp)

    override suspend fun getForecastByDateAndCityId(dt: Long, cityId: Int): WeatherEntity? =
        localDataSource.getForecastByDateAndCityId(dt,cityId)

    override suspend fun getForecastsForCityInRange(
        cityId: Int,
        startDate: Long,
        endDate: Long
    ): List<WeatherEntity> =
        localDataSource.getForecastsForCityInRange(cityId,startDate,endDate)

    override suspend fun deleteForecastById(dt: Long) =
        localDataSource.deleteForecastById(dt)

    // Fetch from API and Store in Room
    // ----------------------------------------------------------------------
    override suspend fun fetchAndStoreForecastByCityName(cityName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = remoteDataSource.getForecastByCityName(cityName)

            if (result.isSuccess) {
                val forecastResponse = result.getOrNull() ?: return@withContext false

                var weatherData = forecastResponse.list.map {
                    WeatherEntity(forecastResponse.city.id,
                        it.dt,
                        it.visibility,
                        it.pop,
                        it.dt_txt,
                        it.main,
                        it.weather,
                        it.clouds,
                        it.wind,
                        it.sys)
                        //it.rain)
                }

                // Insert city
                localDataSource.insertFavCity(forecastResponse.city)

                // Clear old forecasts (before today)
                val todayStartTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis/1000
                localDataSource.deleteForecastById(todayStartTime)

                // Insert new forecasts
                weatherData.forEach { weatherEntity ->
                    localDataSource.insertWeather(weatherEntity)
                }

                Log.d("WeatherRepository", "Forecast fetched and saved for $cityName")
                return@withContext true
            } else {
                Log.e("WeatherRepository", "API result failed for $cityName: ${result.exceptionOrNull()?.message}")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Exception in fetchForecastByCityName: ${e.message}")
            return@withContext false
        }
    }


    // Load forecast from Room for city name
    // ----------------------------------------------------------------------
    override suspend fun loadForecastFromDbByCityName(cityName: String): ForecastResponse? = withContext(Dispatchers.IO){
        try {
            val city = localDataSource.getCityByName(cityName)
                if(city != null)
                {
                    val forecastList = localDataSource.getForecastsForCityInRange(
                        cityId = city.id,
                        startDate = 0L,
                        endDate = Long.MAX_VALUE
                    )

                    ForecastResponse(
                        cod = "200",
                        message = 0,
                        cnt = forecastList.size,
                        city = city,
                        list = forecastList
                    )
                } else {
                    Log.i("WeatherRepository", "City not found in DB for name: $cityName")
                    return@withContext null
                }
            } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching local forecast: ${e.message}")
            return@withContext null
        }
    }

}