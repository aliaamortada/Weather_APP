package com.example.weatherapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weatherapp.databinding.ActivityGetLocationBinding
import com.example.weatherapp.utils.UnitManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*

class GetLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGetLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val REQUEST_CODE_MAP = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.getLocationButton.setOnClickListener {
            showMethodSnackbar()
        }
    }

    // ✅ Show custom snackbar to choose Manual or GPS
    private fun showMethodSnackbar() {
        val view = LayoutInflater.from(this).inflate(R.layout.snackbar_custom_location, null)
        val popupWindow = PopupWindow(view, 900, LinearLayout.LayoutParams.WRAP_CONTENT, true)

        val title = view.findViewById<TextView>(R.id.titleText)
        val message = view.findViewById<TextView>(R.id.messageText)
        val btnLeft = view.findViewById<Button>(R.id.btnDeny)
        val btnRight = view.findViewById<Button>(R.id.btnAllow)



        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)

        btnLeft.setOnClickListener {
            popupWindow.dismiss()
            openMapPicker()
        }

        btnRight.setOnClickListener {
            popupWindow.dismiss()
            checkLocationPermission()
        }
    }

    // ✅ Check permission when GPS selected
    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // ✅ Launcher for requesting permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            showPermissionDeniedSnackbar()
        }
    }

    // ✅ Show snackbar if permission denied
    private fun showPermissionDeniedSnackbar() {
        val view = LayoutInflater.from(this).inflate(R.layout.snackbar_custom_location, null)
        val popupWindow = PopupWindow(view, 900, LinearLayout.LayoutParams.WRAP_CONTENT, true)

        val title = view.findViewById<TextView>(R.id.titleText)
        val message = view.findViewById<TextView>(R.id.messageText)
        val btnLeft = view.findViewById<Button>(R.id.btnDeny)
        val btnRight = view.findViewById<Button>(R.id.btnAllow)

//        title.text = "Permission Required"
//        message.text = "Location permission is required to access GPS."
//        btnLeft.text = "Cancel"
//        btnRight.text = "Retry"

        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)

        btnLeft.setOnClickListener { popupWindow.dismiss() }

        btnRight.setOnClickListener {
            popupWindow.dismiss()
            checkLocationPermission()
        }
    }

    // ✅ Get current location using GPS
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val city = getCityName(it.latitude, it.longitude)
                goToHome(city, it.latitude, it.longitude)
            } ?: Toast.makeText(this, R.string.Locationunavailable, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, R.string.Failedtogetlocation, Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Reverse geocode to get city name
    private fun getCityName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val address = geocoder.getFromLocation(lat, lon, 1)
            address?.firstOrNull()?.adminArea ?: "Unknown"
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }

    }

    // ✅ Start MapPickerActivity for manual selection
    private fun openMapPicker() {
        val intent = Intent(this, MapPickerActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_MAP)
    }

    // ✅ Receive location from manual picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MAP && resultCode == Activity.RESULT_OK) {
            val city = data?.getStringExtra("cityName") ?: "Unknown"
            val lat = data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            val lon = data?.getDoubleExtra("longitude", 0.0) ?: 0.0
            goToHome(city, lat, lon)
            Log.i("city name","city")
            Log.i("lat","lat")
            Log.i("lon name","lon")
        }
    }

    // ✅ Send data to MainActivity → HomeFragment
    private fun goToHome(city: String, lat: Double, lon: Double) {


        UnitManager.saveCityName(this,city)
        UnitManager.saveLatitude(this,lat)
        UnitManager.saveLongitude(this,lon)

        val prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE).edit()
        val isFirstRun = prefs.putBoolean("isFirstRun", false).apply()
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("cityName", city)
            putExtra("latitude", lat)
            putExtra("longitude", lon)
        }
        startActivity(intent)
        finish()
    }
}
