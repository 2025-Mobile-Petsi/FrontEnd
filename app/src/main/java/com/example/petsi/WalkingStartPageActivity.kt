package com.example.petsi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.petsi.databinding.ActivityWalkingStartPageBinding
import com.example.petsi.weather.GpsConverter
import com.example.petsi.weather.RetrofitInstance
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*

class WalkingStartPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalkingStartPageBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val API_KEY = "D6TgfodVmSpfkFCyBF5h5hNquQXtaNGTMnujeY7J9T3KjwJy3tNGVo8Khp697nHxpPj7mhwsKzfnGhjf87YdXw%3D%3D"
    private val TAG = "WEATHER_APP"

    data class WeatherCache(
        val region: String,
        val baseDate: String,
        val baseTime: String,
        val temp: String,
        val sky: String,
        val pop: String,
        val tmx: String,
        val tmn: String
    )

    companion object {
        var cachedWeather: WeatherCache? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalkingStartPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val dateFormat = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN)
        val timeFormat = SimpleDateFormat("a h:mm", Locale.KOREAN)
        binding.weatherSection.textDate.text = dateFormat.format(calendar.time)
        binding.weatherSection.textTime.text = getString(R.string.text_time, timeFormat.format(calendar.time))

        binding.toolbar.logoHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }

        val intentHandler: () -> Unit = {
            val intent = Intent(this, WalkingWithMapActivity::class.java)
            val weatherText = skyToText(cachedWeather?.sky)
            intent.putExtra("weather", weatherText)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }

        binding.walkingState.emptyGo.setOnClickListener { intentHandler() }
        binding.buttonStartWalking.setOnClickListener { intentHandler() }

        binding.bottomNav.bottomNavigationView.selectedItemId = R.id.nav_walk
        binding.bottomNav.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    true
                }
                R.id.nav_walk -> true
                else -> false
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val (baseDate, baseTime) = getBaseDateTime()
        if (cachedWeather?.baseDate == baseDate && cachedWeather?.baseTime == baseTime) {
            displayCachedWeather()
        } else {
            loadWeather(baseDate, baseTime)
        }

        binding.weatherSection.textRefresh.setOnClickListener {
            val (refreshDate, refreshTime) = getBaseDateTime()
            loadWeather(refreshDate, refreshTime)
        }
    }

    private fun loadWeather(baseDate: String, baseTime: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            return
        }

        binding.weatherSection.root.visibility = View.GONE
        binding.weatherLoadingLayout.root.visibility = View.VISIBLE

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(1000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val geocoder = Geocoder(this@WalkingStartPageActivity, Locale.KOREAN)
                val finalRegion = try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    addresses?.firstOrNull()?.let { address ->
                        address.subLocality ?: address.thoroughfare ?: address.locality ?: address.adminArea ?: "알 수 없음"
                    } ?: "알 수 없음"
                } catch (e: Exception) {
                    "알 수 없음"
                }

                val grid = GpsConverter.toGrid(location.latitude, location.longitude)
                RetrofitInstance.api.getVillageForecast(
                    serviceKey = API_KEY,
                    numOfRows = 1000,
                    pageNo = 1,
                    dataType = "JSON",
                    baseDate = baseDate,
                    baseTime = baseTime,
                    nx = grid.x,
                    ny = grid.y
                ).enqueue(object : retrofit2.Callback<com.example.petsi.model.WeatherResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<com.example.petsi.model.WeatherResponse>,
                        response: retrofit2.Response<com.example.petsi.model.WeatherResponse>
                    ) {
                        if (response.isSuccessful) {
                            val items = response.body()?.response?.body?.items?.item
                            cachedWeather = WeatherCache(
                                region = finalRegion,
                                baseDate = baseDate,
                                baseTime = baseTime,
                                temp = items?.find { it.category == "TMP" }?.fcstValue ?: "-",
                                sky = items?.find { it.category == "SKY" }?.fcstValue ?: "-",
                                pop = items?.find { it.category == "POP" }?.fcstValue ?: "-",
                                tmx = items?.find { it.category == "TMX" }?.fcstValue ?: "-",
                                tmn = items?.find { it.category == "TMN" }?.fcstValue ?: "-"
                            )
                            displayCachedWeather()
                        } else {
                            binding.weatherLoadingLayout.root.visibility = View.GONE
                            binding.weatherSection.root.visibility = View.GONE
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.example.petsi.model.WeatherResponse>,
                        t: Throwable
                    ) {
                        binding.weatherLoadingLayout.root.visibility = View.GONE
                        binding.weatherSection.root.visibility = View.GONE
                    }
                })
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun displayCachedWeather() {
        val data = cachedWeather ?: return
        binding.weatherSection.textRegion.text = getString(R.string.text_region, data.region)
        binding.weatherSection.textTemp.text = getString(R.string.text_temperature, data.temp)
        binding.weatherSection.textSky.text = getString(R.string.text_sky, skyToText(data.sky))
        binding.weatherSection.textTmx.text = getString(R.string.text_tmx, data.tmx)
        binding.weatherSection.textTmn.text = getString(R.string.text_tmn, data.tmn)
        binding.weatherSection.textPop.text = getString(R.string.text_pop, data.pop)
        binding.weatherLoadingLayout.root.visibility = View.GONE
        binding.weatherSection.root.visibility = View.VISIBLE
    }

    private fun getBaseDateTime(): Pair<String, String> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREAN)
        val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
        val availableTimes = listOf(2, 5, 8, 11, 14, 17, 20, 23)
        val baseHour = availableTimes.lastOrNull { it <= nowHour } ?: 23
        if (nowHour < 2) calendar.add(Calendar.DATE, -1)
        val baseDate = dateFormat.format(calendar.time)
        val baseTime = String.format("%02d00", baseHour)
        return baseDate to baseTime
    }

    private fun skyToText(value: String?): String {
        return when (value) {
            "1" -> "맑음"
            "3" -> "구름 많음"
            "4" -> "흐림"
            else -> "정보 없음"
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
    }
}
