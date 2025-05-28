package com.example.weatherapp.model.pojo

import com.google.gson.annotations.SerializedName

data class Clouds(
    @SerializedName("all") val all: Int
)