package com.example.weatherapp.ui.favourite

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.databinding.ActivitySearchFavCityBinding
import com.example.weatherapp.model.local.WeatherDatabase
import com.example.weatherapp.model.local.WeatherLocalDataSourceImpl
import com.example.weatherapp.model.remote.RetrofitClient
import com.example.weatherapp.model.remote.WeatherRemoteDataSourceImpl
import com.example.weatherapp.model.repository.WeatherRepositoryImpl
import com.example.weatherapp.utils.UnitManager

class SearchFavCityActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchFavCityBinding
    private lateinit var viewModel: FavoriteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UnitManager.applyLocale(this, UnitManager.getLanguage(this))
        binding = ActivitySearchFavCityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Setup ViewModel
        val factory = FavoriteViewModelFactory(
            WeatherRepositoryImpl(
                WeatherLocalDataSourceImpl(
                    WeatherDatabase.getInstance(this).favCityDao(),
                    WeatherDatabase.getInstance(this).weatherDao()
                ),
                WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService)
            )
        )
        viewModel = ViewModelProvider(this, factory)[FavoriteViewModel::class.java]

        // Search listener
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    val normalizedQuery = query.trim()
                    viewModel.fetchAndStoreCity(normalizedQuery)
                } else {
                    binding.cityResultText.visibility = android.view.View.GONE
                }
                return true
            }


            override fun onQueryTextChange(newText: String?): Boolean {
                // You can keep this empty or use to clear results if needed
                return true
            }
        })


        // Observe result
        viewModel.city.observe(this) { city ->
            if (city != null) {
                binding.cityResultText.apply {
                    text = city.name
                    visibility = android.view.View.VISIBLE
                    setOnClickListener {

                        finish()

                    }
                }
            } else {
                binding.cityResultText.visibility = android.view.View.GONE
            }
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        // **Add this new observer here, after setting up ViewModel and above observers**
        viewModel.fetchStoreSuccess.observe(this) { success ->
            if (success) {
                val cityName = viewModel.city.value?.name
                if (!cityName.isNullOrBlank()) {
                    val intent = Intent(this, FavouriteFragment::class.java)
                    intent.putExtra("cityName", cityName)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }


}
