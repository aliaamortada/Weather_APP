package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.model.local.WeatherDatabase
import com.example.weatherapp.model.remote.RetrofitClient
import com.example.weatherapp.ui.home.HomeFragment
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.weatherapp.model.local.WeatherLocalDataSource
import com.example.weatherapp.model.local.WeatherLocalDataSourceImpl
import com.example.weatherapp.model.remote.WeatherRemoteDataSource
import com.example.weatherapp.model.remote.WeatherRemoteDataSourceImpl
import com.example.weatherapp.model.repository.WeatherRepository
import com.example.weatherapp.model.repository.WeatherRepositoryImpl
import com.example.weatherapp.ui.favourite.FavouriteFragment
import com.example.weatherapp.ui.notification.NotificationFragment
import com.example.weatherapp.ui.settings.SettingsFragment
import com.example.weatherapp.utils.UnitManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        UnitManager.applyLocale(this, UnitManager.getLanguage(this))

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            // Handle menu item clicks
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    toolbar.title = getString(R.string.home)
                }
                R.id.nav_favourite -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FavouriteFragment())
                        .commit()
                    toolbar.title = getString(R.string.favourite)
                }
                R.id.nav_alert -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, NotificationFragment())
                        .commit()
                    toolbar.title = getString(R.string.alert)
                }
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SettingsFragment())
                        .commit()
                    toolbar.title = getString(R.string.settings)
                }

            }
            drawerLayout.closeDrawers() // Close drawer after selection
            true
        }


        // Load the default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home, menu) // Make sure this matches your menu file name
        return true
    }
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(navView)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()

    }

}
