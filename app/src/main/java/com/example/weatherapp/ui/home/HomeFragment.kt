package com.example.weatherapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.model.local.WeatherDatabase
import com.example.weatherapp.model.local.WeatherEntity
import com.example.weatherapp.model.local.WeatherLocalDataSourceImpl
import com.example.weatherapp.model.remote.RetrofitClient
import com.example.weatherapp.model.remote.WeatherRemoteDataSourceImpl
import com.example.weatherapp.model.repository.WeatherRepositoryImpl
import com.example.weatherapp.utils.UnitManager
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var dailyAdapter: DailyAdapter
    private var currentCity: String = "Cairo"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        UnitManager.applyLocale(requireContext(),UnitManager.getLanguage(requireContext()))
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        UnitManager.applyLocale(requireContext(),UnitManager.getLanguage(requireContext()))

        val factory = HomeViewModelFactory(
            WeatherRepositoryImpl(
                WeatherLocalDataSourceImpl(
                    WeatherDatabase.getInstance(requireContext()).favCityDao(),
                    WeatherDatabase.getInstance(requireContext()).weatherDao()
                ),
                WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService)
            )
        )

        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        setupRecyclerViews()
        observeViewModel()
        val savedCity = UnitManager.getCityName(requireContext())
        if (savedCity.isNotBlank() && savedCity != "Unknown city") {
            currentCity = savedCity
            viewModel.fetchAndStoreForecast(currentCity)
        } else {
            // No saved city — show a default city or prompt the user to select one
            val defaultCity = "Cairo" // or any default city you prefer
            currentCity = defaultCity
            UnitManager.saveCityName(requireContext(), defaultCity)  // Save default city
            viewModel.fetchAndStoreForecast(defaultCity)

            // Optionally show a message or UI prompt to user
            Toast.makeText(requireContext(), "No saved city found, loading default city: $defaultCity", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val latestCity = UnitManager.getCityName(requireContext())
        if (latestCity != currentCity) {
            currentCity = latestCity
            viewModel.fetchAndStoreForecast(currentCity)
        }
    }


    private fun setupRecyclerViews() {
        hourlyAdapter = HourlyAdapter()
        dailyAdapter = DailyAdapter()

        binding.rvHourlyForecast.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }

        binding.rvDailyForecast.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dailyAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.forecast.observe(viewLifecycleOwner) { forecast ->
            // Header info
            binding.tvCityName.text = forecast.city.name
            binding.tvCurrentDate.text = formatDate(System.currentTimeMillis())

            forecast.list.firstOrNull()?.let { item ->
                val tempConverted = UnitManager.convertTemperature(item.main.temp, requireContext())
                binding.tvTemperature.text = "${tempConverted.toInt()}°"
                binding.tvWeatherDescription.text = item.weather.firstOrNull()?.description ?: ""
                val iconCode = item.weather.firstOrNull()?.icon ?: "01d"
                val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                Glide.with(this).load(iconUrl).into(binding.imgWeatherIcon)
            }

            // Group hourly forecast by date
            val groupedByDate: Map<String, List<WeatherEntity>> = forecast.list
                .groupBy { it.dt_txt.substringBefore(" ") }

            val firstDayForecastList: List<WeatherEntity>? = groupedByDate.values.firstOrNull()
            hourlyAdapter.submitList(firstDayForecastList!!.sortedBy { it.dt })

            // Daily forecast
            val dailyRanges = forecast.list
                .groupBy { it.dt_txt.substringBefore(" ") }
                .map { (date, itemsForDate) ->
                    val min = itemsForDate.minOf { it.main.tempMin }
                    val max = itemsForDate.maxOf { it.main.tempMax }

                    val representativeItem = itemsForDate.find { it.dt_txt.contains("12:00:00") }
                        ?: itemsForDate.first()

                    val weather = representativeItem.weather.firstOrNull()
                    val description = weather?.description?.replaceFirstChar(Char::uppercaseChar) ?: "N/A"
                    val icon = weather?.icon ?: ""

                    val dayName = try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dateObj = sdf.parse(date)
                        SimpleDateFormat("EEEE", Locale.getDefault()).format(dateObj ?: Date())
                    } catch (e: Exception) {
                        "Unknown"
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

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
