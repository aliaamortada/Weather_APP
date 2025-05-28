package com.example.weatherapp.model.pojo

import com.example.weatherapp.model.local.FavCityEntity
import com.example.weatherapp.model.local.WeatherEntity
import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("cod") val cod: String,
    @SerializedName("message") val message: Int,
    @SerializedName("cnt") val cnt: Int,
    @SerializedName("list") val list: List<WeatherEntity>,
    @SerializedName("city") val city: FavCityEntity
)