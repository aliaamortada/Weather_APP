package com.example.weatherapp.ui.favourite

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentFavouriteBinding
import com.example.weatherapp.model.local.WeatherDatabase
import com.example.weatherapp.model.local.WeatherLocalDataSourceImpl
import com.example.weatherapp.model.remote.RetrofitClient
import com.example.weatherapp.model.remote.WeatherRemoteDataSourceImpl
import com.example.weatherapp.model.repository.WeatherRepositoryImpl
import com.example.weatherapp.utils.UnitManager


class FavouriteFragment : Fragment() {

    private var _binding: FragmentFavouriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FavoriteViewModel
    private lateinit var adapter: FavCityAdapter

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        UnitManager.applyLocale(requireContext(), UnitManager.getLanguage(requireContext()))
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val factory = FavoriteViewModelFactory(
            WeatherRepositoryImpl(
                WeatherLocalDataSourceImpl(
                    WeatherDatabase.getInstance(requireContext()).favCityDao(),
                    WeatherDatabase.getInstance(requireContext()).weatherDao()
                ),
                WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService)
            )
        )
        viewModel = ViewModelProvider(this, factory)[FavoriteViewModel::class.java]


        setupRecyclerView()

        // FAB opens SearchFavCityActivity
        binding.fabAdd.setOnClickListener {
            if (isOnline(requireContext())) {
                startActivity(Intent(requireContext(), SearchFavCityActivity::class.java)) // ✅ Only navigate if online
            } else {
                Toast.makeText(requireContext(), R.string.youoffline, Toast.LENGTH_SHORT).show() // ✅ Show toast if offline
            }
        }


        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = FavCityAdapter(
            onDeleteClick = { city ->
                viewModel.removeFavCity(requireContext(), city)
            },
            onCityClick = { city -> // ✅ Handle card press
                val intent = Intent(requireContext(), CityDetailsActivity::class.java)
                intent.putExtra("cityName", city.name)
                startActivity(intent)
            }
        )
        binding.rvFavourite.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavourite.adapter = adapter
    }


    private fun observeViewModel() {
        viewModel.allcity.observe(viewLifecycleOwner) { cities ->
            adapter.submitList(cities)
        }
    }
    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    override fun onResume() {
        super.onResume()
        viewModel.loadFavCities(requireContext()) // Reload list when returning
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
