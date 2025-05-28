package com.example.weatherapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.pojo.ForecastResponse
import com.example.weatherapp.model.repository.WeatherRepository
import kotlinx.coroutines.launch


class HomeViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                HomeViewModel(repository) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
}

class HomeViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {

    // LiveData to observe forecast data
    private val _forecast = MutableLiveData<ForecastResponse>()
    val forecast: LiveData<ForecastResponse> = _forecast

    // LiveData for errors
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // LiveData for loading status
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // ----------------------------------------
    // Fetch forecast from API and store it locally
    fun fetchAndStoreForecast(cityName: String) {
        viewModelScope.launch {
            loadForecastFromDb(cityName)
            _isLoading.postValue(true)
            val success = weatherRepository.fetchAndStoreForecastByCityName(cityName)
            _isLoading.postValue(false)
            loadForecastFromDb(cityName)

        }
    }

    // ----------------------------------------
    // Load forecast from Room database
    fun loadForecastFromDb(cityName: String) {
        viewModelScope.launch {
            val localData = weatherRepository.loadForecastFromDbByCityName(cityName)
            if (localData != null) {
                _forecast.postValue(localData)
            } else {
                _error.postValue("No cached data found for $cityName")
            }
        }
    }

    // ----------------------------------------
    // Optional direct API fetch (without storing)
    fun getForecast(lat: Double, lon: Double) {
        viewModelScope.launch {
            val result = weatherRepository.getForecast(lat, lon)
            result.onSuccess { response ->
                _forecast.postValue(response)
            }.onFailure { exception ->
                _error.postValue(exception.message ?: "An unknown error occurred")
            }
        }
    }

    // Optional: API fetch by city name (without storing)
    fun getForecastByCityName(cityName: String) {
        viewModelScope.launch {
            val result = weatherRepository.getForecastByCityName(cityName)
            result.onSuccess { response ->
                _forecast.postValue(response)
            }.onFailure { exception ->
                _error.postValue(exception.message ?: "An unknown error occurred")
            }
        }
    }
}
