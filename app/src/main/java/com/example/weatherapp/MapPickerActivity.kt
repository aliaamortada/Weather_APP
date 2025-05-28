package com.example.weatherapp

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.model.local.WeatherDatabase
import com.example.weatherapp.model.local.WeatherLocalDataSourceImpl
import com.example.weatherapp.model.remote.RetrofitClient
import com.example.weatherapp.model.remote.WeatherRemoteDataSourceImpl
import com.example.weatherapp.model.repository.WeatherRepositoryImpl
import com.example.weatherapp.utils.UnitManager.saveCityName
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.util.*

class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    companion object {
        private const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        mapView = findViewById(R.id.mapView)

        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        val defaultLocation = LatLng(30.0444, 31.2357) // Cairo
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))

        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))

            val repository = WeatherRepositoryImpl(
                WeatherLocalDataSourceImpl(
                    WeatherDatabase.getInstance(this).favCityDao(),
                    WeatherDatabase.getInstance(this).weatherDao()
                ),
                WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService)
            )

            lifecycleScope.launch {
                var date =  repository.getForecast(latLng.latitude, latLng.longitude)
                if(date.isSuccess) {
                    Log.i("succ","ay 7aga  "  + date.getOrNull()!!.city.name)

                    saveCityName(this@MapPickerActivity, date.getOrNull()!!.city.name)
                    Toast.makeText(this@MapPickerActivity,"$date.getOrNull()!!.city.name", Toast.LENGTH_SHORT).show()

                    val resultIntent = Intent(this@MapPickerActivity,MainActivity::class.java).apply {
                        putExtra("cityName", date.getOrNull()!!.city.name)
                        putExtra("latitude", latLng.latitude)
                        putExtra("longitude", latLng.longitude)
                    }
                    startActivity(resultIntent)
                    // setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                // Use forecast data here
            }



        }
    }

    // Lifecycle methods for MapView
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onStop() { super.onStop(); mapView.onStop() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onDestroy() { mapView.onDestroy(); super.onDestroy() }
    override fun onLowMemory() { mapView.onLowMemory(); super.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }
}
