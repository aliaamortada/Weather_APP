package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityGetLocationBinding
import com.example.weatherapp.ui.favourite.CityDetailsActivity
import com.example.weatherapp.utils.UnitManager
import com.google.android.gms.location.LocationServices

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay for 3 seconds, then go to main activity
        Handler(Looper.getMainLooper()).postDelayed({


                val prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE)
                val isFirstRun = prefs.getBoolean("isFirstRun", true)

                if (isFirstRun) {


                    startActivity(Intent(this, GetLocationActivity::class.java))

                }
            else
                {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("cityName", UnitManager.getCityName(this@SplashActivity))
                        putExtra("latitude", UnitManager.getLatitude(this@SplashActivity))
                        putExtra("longitude", UnitManager.getLongitude(this@SplashActivity))
                    }
                    startActivity(intent)
                    finish()
                }


            finish()
        }, 3000)
    }
}
