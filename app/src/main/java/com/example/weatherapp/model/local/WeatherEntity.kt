package com.example.weatherapp.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.weatherapp.model.pojo.Clouds
import com.example.weatherapp.model.pojo.Main
import com.example.weatherapp.model.pojo.Rain
import com.example.weatherapp.model.pojo.Sys
import com.example.weatherapp.model.pojo.Weather
import com.example.weatherapp.model.pojo.Wind
import com.google.gson.Gson

@Entity(tableName = "weather_forecast",
    primaryKeys = ["cityId","dt"])
data class WeatherEntity(

    var cityId: Int,
    val dt: Long,  // timestamp as unique identifier
    val visibility: Int,
    val pop: Double,
    val dt_txt: String,

    @Embedded(prefix = "main_")
    val main: Main,

    @TypeConverters(Convert::class)
    val weather: List<Weather>,

    @Embedded(prefix = "clouds_")
    val clouds: Clouds,

    @Embedded(prefix = "wind_")
    val wind: Wind,

    @Embedded(prefix = "sys_")
    val sys: Sys? = null,
    // Rain and weather list need converters
    //val rain: Double, // from rain.3h if exists
)



