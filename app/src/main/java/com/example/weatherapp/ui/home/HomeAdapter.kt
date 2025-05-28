package com.example.weatherapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.ItemDailyForecastBinding
import com.example.weatherapp.databinding.ItemHourlyForecastBinding
import com.example.weatherapp.model.local.WeatherEntity
import java.text.SimpleDateFormat
import java.util.*

class HourlyAdapter : RecyclerView.Adapter<HourlyAdapter.HourlyViewHolder>() {

    private var items: List<WeatherEntity> = emptyList()

    fun submitList(list: List<WeatherEntity>) {
        items = list
        notifyDataSetChanged()
    }

    inner class HourlyViewHolder(private val binding: ItemHourlyForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WeatherEntity) {
            val hourFormat = SimpleDateFormat("hh a", Locale.getDefault())
            val time = hourFormat.format(item.dt * 1000L)
            binding.tvHour.text = time
            binding.tvHourlyTemperature.text = "${item.main.temp.toInt()}°"
            val iconUrl = "https://openweathermap.org/img/wn/${item.weather[0].icon}@2x.png"
            Glide.with(binding.root).load(iconUrl).into(binding.imgHourlyWeatherIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val binding = ItemHourlyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}

class DailyAdapter : RecyclerView.Adapter<DailyAdapter.DailyViewHolder>() {

    private var items: List<DailyTemperatureRange> = emptyList()

    fun submitList(list: List<DailyTemperatureRange>) {
        items = list
        notifyDataSetChanged()
    }

    inner class DailyViewHolder(private val binding: ItemDailyForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DailyTemperatureRange) {
            binding.tvDayName.text = item.dayName
            binding.tvDailyTemperature.text = "${item.tempMin.toInt()}°"
            binding.tvWeatherDescription.text = item.description
            val iconUrl = "https://openweathermap.org/img/wn/${item.icon}@2x.png"
            Glide.with(binding.root).load(iconUrl).into(binding.imgDailyWeatherIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val binding = ItemDailyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
