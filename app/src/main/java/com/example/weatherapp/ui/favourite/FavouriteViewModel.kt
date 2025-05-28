package com.example.weatherapp.ui.favourite

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.local.FavCityEntity
import com.example.weatherapp.model.pojo.ForecastResponse
import com.example.weatherapp.model.repository.WeatherRepository
import com.example.weatherapp.utils.UnitManager
import kotlinx.coroutines.launch

class FavoriteViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            FavoriteViewModel(repository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

class FavoriteViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {

    private val _forecast = MutableLiveData<ForecastResponse>()
    val forecast: LiveData<ForecastResponse> = _forecast

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _allcity = MutableLiveData<List<FavCityEntity> >()
    val allcity: LiveData<List<FavCityEntity>> = _allcity


    private val _city = MutableLiveData<FavCityEntity >()
    val city: LiveData<FavCityEntity> = _city

    private val _fetchStoreSuccess = MutableLiveData<Boolean>()
    val fetchStoreSuccess: LiveData<Boolean> = _fetchStoreSuccess



    fun loadFavCities(contxt : Context) {
        viewModelScope.launch {
            _allcity.postValue(weatherRepository.getAllFavCities().filter { it.name != UnitManager.getCityName(contxt)} )
        }
    }

    fun removeFavCity(contxt : Context,city: FavCityEntity) {
        viewModelScope.launch {
            weatherRepository.deleteFavCity(city)
            loadFavCities(contxt) // Refresh after deletion
        }
    }
    // ----------------------------------------
    fun getCityByName(name: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val remoteResult = weatherRepository.getForecastByCityName(name.trim())


                if (remoteResult.isSuccess) {
                    val forecastResponse = remoteResult.getOrNull()

                    if (forecastResponse != null) {
                        _city.postValue(forecastResponse.city)
                        _error.postValue("")
                    } else {
                        _error.postValue("City not found remotely: $name")
                    }
                } else {
                    _error.postValue("Error fetching city remotely: ${remoteResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.i("Exception" ,"${e.localizedMessage ?: e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    fun fetchAndStoreCity(cityName: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val success = weatherRepository.fetchAndStoreForecastByCityName(cityName)
            _isLoading.postValue(false)

            _fetchStoreSuccess.postValue(success)
            if (success) {
                // Update city LiveData with the stored city entity
                val cities = weatherRepository.getAllFavCities()
                val city = cities.find { it.name.equals(cityName, ignoreCase = true) }
                city?.let { _city.postValue(it) }
            } else {
                _error.postValue("Failed to fetch and store city: $cityName")
            }
        }
    }


    // ----------------------------------------
    // Fetch forecast from API and save to Room
    fun fetchAndStoreForecast(cityName: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val success = weatherRepository.fetchAndStoreForecastByCityName(cityName)
            _isLoading.postValue(false)

            if (success) {
                loadForecastFromDb(cityName)
            } else {
                _error.postValue("Failed to fetch and store forecast for $cityName")
            }
        }
    }

    // ----------------------------------------
    // Load cached forecast from Room
    fun loadForecastFromDb(cityName: String) {
        viewModelScope.launch {
            val localData = weatherRepository.loadForecastFromDbByCityName(cityName)
            if (localData != null) {
                _forecast.postValue(localData)
            } else {
                _error.postValue("No local forecast data for $cityName")
            }
        }
    }


    // Insert city to favorites (DB)
    private suspend fun insertFavCity(city: FavCityEntity) {
        weatherRepository.insertFavCity(city)
    }

    // Delete city from favorites (DB)
    private suspend fun deleteFavCity(city: FavCityEntity) {
        weatherRepository.deleteFavCity(city)
    }



}
