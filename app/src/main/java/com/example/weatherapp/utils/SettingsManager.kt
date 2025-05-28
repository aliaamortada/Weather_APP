package com.example.weatherapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

object UnitManager {

    private const val PREFS_NAME = "weather_app_units"
    private const val KEY_TEMPERATURE_UNIT = "temperature_unit"
    private const val KEY_WIND_SPEED_UNIT = "wind_speed_unit"
    private const val KEY_CITY_NAME = "city_name"
    private const val KEY_LATITUDE = "latitude"
    private const val KEY_LONGITUDE = "longitude"
    private const val KEY_LANGUAGE = "language"

    // Save city name to SharedPreferences
    fun saveCityName(context: Context, cityName: String) {
        getPrefs(context).edit().putString(KEY_CITY_NAME, cityName).apply()
    }
    fun applyLocale(context: Context, langCode: String) {
        val resources = context.resources
        val dm = resources.displayMetrics
        val config: Configuration = resources.configuration
        config.setLocale(Locale(langCode))
        context.resources.updateConfiguration(config, dm)
    }
    // Get saved city name or default to "Unknown city"
    fun getCityName(context: Context): String {
        return getPrefs(context).getString(KEY_CITY_NAME, "Unknown city") ?: "Unknown city"
    }

    // Save latitude to SharedPreferences
    fun saveLatitude(context: Context, latitude: Double) {
        getPrefs(context).edit().putString(KEY_LATITUDE, latitude.toString()).apply()
    }

    // Get saved latitude or default to 0.0
    fun getLatitude(context: Context): Double {
        return getPrefs(context).getString(KEY_LATITUDE, "0.0")?.toDoubleOrNull() ?: 0.0
    }

    // Save longitude to SharedPreferences
    fun saveLongitude(context: Context, longitude: Double) {
        getPrefs(context).edit().putString(KEY_LONGITUDE, longitude.toString()).apply()
    }

    // Get saved longitude or default to 0.0
    fun getLongitude(context: Context): Double {
        return getPrefs(context).getString(KEY_LONGITUDE, "0.0")?.toDoubleOrNull() ?: 0.0
    }
    // Save temperature unit to SharedPreferences
    fun saveTemperatureUnit(context: Context, unit: String) {
        getPrefs(context).edit().putString(KEY_TEMPERATURE_UNIT, unit).apply()
    }

    // Get saved temperature unit or default to "Celsius"
    fun getTemperatureUnit(context: Context): String {
        return getPrefs(context).getString(KEY_TEMPERATURE_UNIT, "Celsius") ?: "Celsius"
    }

    // Save wind speed unit to SharedPreferences
    fun saveWindSpeedUnit(context: Context, unit: String) {
        getPrefs(context).edit().putString(KEY_WIND_SPEED_UNIT, unit).apply()
    }

    // Get saved wind speed unit or default to "Meter/sec"
    fun getWindSpeedUnit(context: Context): String {
        return getPrefs(context).getString(KEY_WIND_SPEED_UNIT, "Meter/sec") ?: "Meter/sec"
    }

    // Convert temperature to user's preferred unit
    fun convertTemperature(valueInCelsius: Double, context: Context): Double {
        return when (getTemperatureUnit(context)) {
            "Fahrenheit" -> valueInCelsius * 9 / 5 + 32
            "Kelvin" -> valueInCelsius + 273.15
            else -> valueInCelsius  // Default Celsius
        }
    }
    // Save language code to SharedPreferences
    fun saveLanguage(context: Context, langCode: String) {
        getPrefs(context).edit().putString(KEY_LANGUAGE, langCode).apply()
    }

    // Get saved language code or default to "en"
    fun getLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE, "en") ?: "en"
    }

    // Convert wind speed to user's preferred unit
    fun convertWindSpeed(valueInMeterPerSec: Double, context: Context): Double {
        return when (getWindSpeedUnit(context)) {
            "Km/h" -> valueInMeterPerSec * 3.6
            else -> valueInMeterPerSec
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}

