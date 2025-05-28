package com.example.weatherapp.model.local

import androidx.room.TypeConverter
import com.example.weatherapp.model.pojo.Weather
import com.google.gson.Gson

public class Convert {
    @TypeConverter
    public fun fromWeatherList(value: List<Weather>): String =
        Gson().toJson(value)

    @TypeConverter
    public fun toWeatherList(value: String): List<Weather> =
        Gson().fromJson(value, Array<Weather>::class.java).toList()
}