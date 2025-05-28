package com.example.weatherapp.model.local

import androidx.room.*

@Dao
interface FavCityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavCity(city: FavCityEntity)

    @Delete
    suspend fun deleteFavCity(city: FavCityEntity)

    @Query("SELECT * FROM fav_cities")
    suspend fun getAllFavCities(): List<FavCityEntity>

    @Query("SELECT * FROM fav_cities WHERE  name LIKE :name")
    suspend fun getCityByName(name: String): FavCityEntity?

    @Query("SELECT * FROM fav_cities WHERE id = :cityId LIMIT 1")
    suspend fun getCityById(cityId: Int): FavCityEntity?
}
