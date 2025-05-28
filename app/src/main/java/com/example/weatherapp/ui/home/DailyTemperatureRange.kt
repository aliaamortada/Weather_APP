package com.example.weatherapp.ui.home

import com.google.gson.annotations.SerializedName

data class DailyTemperatureRange(
    val dayName: String,         // e.g., "Monday"
    val date: String,            // Original date string, e.g., "2025-05-26"
    val tempMin: Double,
    val tempMax: Double,
    val description: String,     // e.g., "Light rain"
    val icon: String
)