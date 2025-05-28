package com.example.weatherapp.ui.favourite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.model.local.FavCityEntity

class FavCityAdapter(
    private val onDeleteClick: (FavCityEntity) -> Unit,
    private val onCityClick: (FavCityEntity) -> Unit
) : ListAdapter<FavCityEntity, FavCityAdapter.FavCityViewHolder>(FavDiffCallback()) {

    inner class FavCityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCityName: TextView = itemView.findViewById(R.id.tvCityName)
        private val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)

        fun bind(city: FavCityEntity) {
            tvCityName.text = city.name
            ivDelete.setOnClickListener { onDeleteClick(city) }
            itemView.setOnClickListener { onCityClick(city) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavCityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fav_city_widget, parent, false)
        return FavCityViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavCityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FavDiffCallback : DiffUtil.ItemCallback<FavCityEntity>() {
        override fun areItemsTheSame(oldItem: FavCityEntity, newItem: FavCityEntity) = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: FavCityEntity, newItem: FavCityEntity) = oldItem == newItem
    }
}
