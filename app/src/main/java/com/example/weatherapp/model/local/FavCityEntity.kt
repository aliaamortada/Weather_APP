package com.example.weatherapp.model.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weatherapp.model.pojo.Coord
import com.google.gson.annotations.SerializedName

// store fav cities in room

@Entity(tableName = "fav_cities")
data class FavCityEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val country: String,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long,

    // @Embedded -> provided by room to handle saving object in same table for another one
    @Embedded(prefix = "coord_")
    val coord: Coord
)


