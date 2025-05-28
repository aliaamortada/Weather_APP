package com.example.weatherapp.ui.home



import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runners.JUnit4
import com.example.weatherapp.model.pojo.ForecastResponse
import com.example.weatherapp.model.repository.WeatherRepository
import com.example.weatherapp.utils.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class HomeViewModelTest {

 private lateinit var viewModel: HomeViewModel
 private lateinit var repository: WeatherRepository
 private lateinit var context: Application

 @Before
 fun setup() {
  repository = mockk(relaxed = true)
  viewModel = HomeViewModel(repository)
 }

 @Test
 fun fetchAndStoreForecast_success_loadsData() = runTest {
  val cityName = "Cairo"
  val forecast = mockk<ForecastResponse>()

  coEvery { repository.loadForecastFromDbByCityName(cityName) } returns forecast
  coEvery { repository.fetchAndStoreForecastByCityName(cityName) } returns true

  viewModel.fetchAndStoreForecast(cityName)

  val result = viewModel.forecast.getOrAwaitValue()
  assertThat(result, `is`(forecast))
 }

 @Test
 fun loadForecastFromDb_noData_setsError() = runTest {
  val cityName = "Nowhere"

  coEvery { repository.loadForecastFromDbByCityName(cityName) } returns null

  viewModel.loadForecastFromDb(cityName)

  val errorResult = viewModel.error.getOrAwaitValue()
  assertThat(errorResult, `is`("No cached data found for $cityName"))
 }

 @Test
 fun getForecast_success_updatesForecast() = runTest {
  val forecast = mockk<ForecastResponse>()

  coEvery { repository.getForecast(30.0, 31.0) } returns Result.success(forecast)

  viewModel.getForecast(30.0, 31.0)

  val result = viewModel.forecast.getOrAwaitValue()
  assertThat(result, `is`(forecast))
 }

 @Test
 fun getForecast_failure_setsError() = runTest {
  val message = "Network error"

  coEvery { repository.getForecast(0.0, 0.0) } returns Result.failure(Exception(message))

  viewModel.getForecast(0.0, 0.0)

  val error = viewModel.error.getOrAwaitValue()
  assertThat(error, `is`(message))
 }
}
