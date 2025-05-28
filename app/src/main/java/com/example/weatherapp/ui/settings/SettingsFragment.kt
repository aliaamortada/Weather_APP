package com.example.weatherapp.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.MapPickerActivity
import com.example.weatherapp.R
import com.example.weatherapp.model.local.WeatherDatabase
import com.example.weatherapp.model.local.WeatherLocalDataSourceImpl
import com.example.weatherapp.model.remote.RetrofitClient
import com.example.weatherapp.model.remote.WeatherRemoteDataSourceImpl
import com.example.weatherapp.model.repository.WeatherRepositoryImpl
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.weatherapp.utils.UnitManager
import com.example.weatherapp.utils.UnitManager.saveCityName
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsFragment : Fragment() {

    // Name of SharedPreferences file to save user settings persistently (non-unit related)
    private val PREFS_NAME = "weather_app_settings"

    // RadioGroups corresponding to each setting in the layout
    private lateinit var rgTemperature: RadioGroup
    private lateinit var rgLanguage: RadioGroup
    private lateinit var rgLocation: RadioGroup
    private lateinit var rgNotification: RadioGroup
    private lateinit var rgWindSpeed: RadioGroup

    // Permission launcher for requesting GPS permission (ACCESS_FINE_LOCATION)
    private val gpsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(requireContext(), R.string.GPSpermissiongranted, Toast.LENGTH_SHORT).show()
                Log.i("SettingsFragment", "GPS permission granted - update location from GPS")
                // Call your GPS location update method here
                updateLocationFromGPS()
            } else {
                Toast.makeText(requireContext(), R.string.GPSpermissiondenied, Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        UnitManager.applyLocale(requireContext(),UnitManager.getLanguage(requireContext()))
        // Inflate the layout XML for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the RadioGroups by finding them from the view
        rgTemperature = view.findViewById(R.id.rg_temperature)
        rgLanguage = view.findViewById(R.id.rg_language)
        rgLocation = view.findViewById(R.id.rg_location)
        rgNotification = view.findViewById(R.id.rg_notification)
        rgWindSpeed = view.findViewById(R.id.rg_wind_speed)

        // Load saved preferences and update UI accordingly
        loadPreferences()

        // Set up listeners for each RadioGroup to handle user changes
        setupListeners()
    }

    private fun setupListeners() {
        // Listener for Temperature RadioGroup
        rgTemperature.setOnCheckedChangeListener { _, checkedId ->
            val selected = view?.findViewById<RadioButton>(checkedId)?.text.toString()
            // Save temperature unit using UnitManager
            UnitManager.saveTemperatureUnit(requireContext(), selected)
        }

        rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val selected = view?.findViewById<RadioButton>(checkedId)?.text.toString()

            val langCode = when (selected.lowercase(Locale.getDefault())) {
                "arabic" -> "ar"
                "english" -> "en"
                else -> "en"  // default to English if unknown
            }

            // Save language code and apply locale using UnitManager
            UnitManager.saveLanguage(requireContext(), langCode)
            UnitManager.applyLocale(requireContext(), langCode)

            Log.i("SettingsFragment", "Language changed to $selected ($langCode)")

            // Reload the SettingsFragment to apply the language change
            //requireActivity().recreate()
        }


        // Listener for Location RadioGroup
        rgLocation.setOnCheckedChangeListener { _, checkedId ->
            val selected = view?.findViewById<RadioButton>(checkedId)?.text.toString()
            when (selected) {
                "Map" -> {
                    savePreference("location", selected)
                    val intent = Intent(requireContext(), MapPickerActivity::class.java)
                    startActivity(intent)
                }
                "GPS" -> {
                    savePreference("location", selected)
                    checkGpsPermissionAndUpdate()
                }
            }
        }

        // Listener for Notification RadioGroup
        rgNotification.setOnCheckedChangeListener { _, checkedId ->
            val selected = view?.findViewById<RadioButton>(checkedId)?.text.toString()
            savePreference("notification", selected)
            if (selected == "Enabled") {
                updateLocationFromGPS()
                Log.i("SettingsFragment", "Notifications enabled")
            } else {
                Log.i("SettingsFragment", "Notifications disabled")
            }
        }

        // Listener for Wind Speed RadioGroup
        rgWindSpeed.setOnCheckedChangeListener { _, checkedId ->
            val selected = view?.findViewById<RadioButton>(checkedId)?.text.toString()
            // Save wind speed unit using UnitManager
            UnitManager.saveWindSpeedUnit(requireContext(), selected)
        }
    }

    // Check if GPS permission is granted, request if not, then update location
    private fun checkGpsPermissionAndUpdate() {
        context?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), R.string.UpdatinglocationfromGPS, Toast.LENGTH_SHORT).show()
                Log.i("SettingsFragment", "Permission granted, updating GPS location")
                updateLocationFromGPS()
            } else {
                gpsPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Save a string preference key-value pair in SharedPreferences
    private fun savePreference(key: String, value: String) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }

    // Load all preferences and set the RadioGroups accordingly
    private fun loadPreferences() {
        // Load temperature unit using UnitManager and update RadioGroup
        val tempUnit = UnitManager.getTemperatureUnit(requireContext())
        setCheckedRadioButton(rgTemperature, tempUnit)

        // Load language preference as before
        // Load language code and map to corresponding RadioButton text
        val langCode = UnitManager.getLanguage(requireContext())
        val langText = when (langCode) {
            "ar" -> "Arabic"
            "en" -> "English"
            else -> "English"
        }
        setCheckedRadioButton(rgLanguage, langText)

        // Load location, notification, and wind speed preferences
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        setCheckedRadioButton(rgLocation, prefs.getString("location", "Map"))
        setCheckedRadioButton(rgNotification, prefs.getString("notification", "Enabled"))

        // Load wind speed unit using UnitManager and update RadioGroup
        val windSpeedUnit = UnitManager.getWindSpeedUnit(requireContext())
        setCheckedRadioButton(rgWindSpeed, windSpeedUnit)
    }

    // Method to request a single fresh location update using GPS
    private fun updateLocationFromGPS() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Request permission if needed, then get current location
        gpsPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                Toast.makeText(requireContext(), "$latitude, $longitude", Toast.LENGTH_SHORT).show()
                Log.i("SettingsFragment", "Updated GPS location: lat=$latitude, lon=$longitude")

                val repository = WeatherRepositoryImpl(
                    WeatherLocalDataSourceImpl(
                        WeatherDatabase.getInstance(requireContext()).favCityDao(),
                        WeatherDatabase.getInstance(requireContext()).weatherDao()
                    ),
                    WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService)
                )
             //var date =  repository.getForecast(latitude, longitude)

//                // Use Geocoder to get city name from lat/lon
//                val geocoder = Geocoder(requireContext(), Locale.getDefault())
//                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
//                val cityName = if (addresses != null && addresses.isNotEmpty()) {
//
//                } else {
//                    "Unknown city"
//                }

                lifecycleScope.launch {
                    var date =  repository.getForecast(latitude, longitude)
                    if(date.isSuccess)
                    saveCityName(requireContext(),date.getOrNull()!!.city.name)
                    // Use forecast data here
                }


                // Save to SharedPreferences


            } else {
                Toast.makeText(requireContext(), R.string.Failedtogetlocation, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), R.string.Errorgettinglocation, Toast.LENGTH_SHORT).show()
            Log.e("SettingsFragment", "Error getting location", exception)
        }
    }

    // Helper to check the RadioButton inside a RadioGroup matching the saved value
    private fun setCheckedRadioButton(radioGroup: RadioGroup, value: String?) {
        if (value == null) return
        for (i in 0 until radioGroup.childCount) {
            val child = radioGroup.getChildAt(i)
            if (child is RadioButton && child.text.toString() == value) {
                child.isChecked = true
                break
            }
        }
    }
}
