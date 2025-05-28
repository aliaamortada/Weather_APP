package com.example.weatherapp.ui.favourite

import android.content.Context
import android.content.res.Configuration
import com.example.weatherapp.ui.home.DailyAdapter
import com.example.weatherapp.ui.home.DailyTemperatureRange
import com.example.weatherapp.ui.home.HourlyAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityCityDetailsBinding
import com.example.weatherapp.model.local.WeatherDatabase
import com.example.weatherapp.model.local.WeatherLocalDataSourceImpl
import com.example.weatherapp.model.remote.RetrofitClient
import com.example.weatherapp.model.remote.WeatherRemoteDataSourceImpl
import com.example.weatherapp.model.repository.WeatherRepositoryImpl
import com.example.weatherapp.utils.UnitManager
import java.util.Locale

class CityDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCityDetailsBinding
    private lateinit var viewModel: FavoriteViewModel
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var dailyAdapter: DailyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        UnitManager.applyLocale(this,UnitManager.getLanguage(this))

        binding = ActivityCityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivClose.setOnClickListener {
            finish()
        }


        // Initialize repository and ViewModel via factory
        val repository = WeatherRepositoryImpl(
            WeatherLocalDataSourceImpl(
                WeatherDatabase.getInstance(this).favCityDao(),
                WeatherDatabase.getInstance(this).weatherDao()
            ),
            WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService)
        )
        val factory = FavoriteViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FavoriteViewModel::class.java]

        setupRecyclerViews()
        observeViewModel()

        // Get city name from intent extras
        val cityName = intent.getStringExtra("cityName") ?: "London"
        viewModel.fetchAndStoreForecast(cityName)
        viewModel.loadForecastFromDb(cityName)
    }

    private fun setupRecyclerViews() {
        hourlyAdapter = HourlyAdapter()
        dailyAdapter = DailyAdapter()

        binding.rvHourlyForecast.apply {
            layoutManager = LinearLayoutManager(this@CityDetailsActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }
        binding.rvDailyForecast.apply {
            layoutManager = LinearLayoutManager(this@CityDetailsActivity)
            adapter = dailyAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.forecast.observe(this) { forecast ->
            binding.tvCityName.text = forecast.city.name


            forecast.list.firstOrNull()?.let { item ->
                binding.tvTemperature.text = "${item.main.temp.toInt()}°"
                binding.tvDescription.text = item.weather.firstOrNull()?.description ?: ""
                val iconCode = item.weather.firstOrNull()?.icon ?: "01d"
                val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                Glide.with(this).load(iconUrl).into(binding.ivWeatherIcon)
            }

            val groupedByDate = forecast.list.groupBy { it.dt_txt.substringBefore(" ") }
            val firstDayForecastList = groupedByDate.values.firstOrNull()
            hourlyAdapter.submitList(firstDayForecastList?.sortedBy { it.dt } ?: emptyList())

            val dailyRanges = forecast.list
                .groupBy { it.dt_txt.substringBefore(" ") }
                .map { (date, itemsForDate) ->
                    val min = itemsForDate.minOf { it.main.tempMin }
                    val max = itemsForDate.maxOf { it.main.tempMax }
                    val representativeItem = itemsForDate.find { it.dt_txt.contains("12:00:00") } ?: itemsForDate.first()
                    val weather = representativeItem.weather.firstOrNull()
                    val description = weather?.description?.replaceFirstChar(Char::uppercaseChar) ?: "N/A"
                    val icon = weather?.icon ?: ""


                    val dayName = try {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val dateObj = sdf.parse(date)
                        java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(dateObj ?: java.util.Date())
                    } catch (e: Exception) {
                        "unknowm"
                    }
                    val pressure = representativeItem.main.pressure
                    val seaLevel = representativeItem.main.seaLevel
                    val humidity = representativeItem.main.humidity
                    val speed = representativeItem.wind.speed
                    val all = representativeItem.clouds.all
                    val visibility = representativeItem.visibility

                    binding.tvPressure.text = "$pressure hPa"
                    binding.tvSealevel.text = "$seaLevel hPa"
                    binding.tvHumidity.text = "$humidity%"
                    binding.tvWind.text = "$speed km/h"
                    binding.tvCloud.text = "$all%"
                    binding.tvVisibility.text = "$visibility km"

                    DailyTemperatureRange(
                        dayName = dayName,
                        date = date,
                        tempMin = min,
                        tempMax = max,
                        description = description,
                        icon = icon

                    )
                }
            dailyAdapter.submitList(dailyRanges.drop(1))
        }
    }

}
