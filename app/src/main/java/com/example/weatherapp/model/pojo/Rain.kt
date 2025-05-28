package com.example.weatherapp.model.pojo

import com.google.gson.annotations.SerializedName

data class Rain(
    @SerializedName("3h") val threeH: Double
)