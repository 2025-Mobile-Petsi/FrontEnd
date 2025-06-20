package com.example.petsi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.petsi.databinding.ActivityWalkingStartPageBinding
import com.example.petsi.weather.GpsConverter
import com.example.petsi.weather.RetrofitInstance
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


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

        Log.d(TAG, "onCreate 시작됨")

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val dateFormat = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN)
        val timeFormat = SimpleDateFormat("a h:mm", Locale.KOREAN)
        binding.weatherSection.textDate.text = dateFormat.format(calendar.time)
        binding.weatherSection.textTime.text = "현재 시각 : ${timeFormat.format(calendar.time)}"

        binding.toolbar.logoHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }

        binding.walkingState.emptyGo.setOnClickListener {
            val intent = Intent(this, activity_walking_with_map::class.java)
            val weatherText = skyToText(cachedWeather?.sky)
            intent.putExtra("weather", weatherText)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }

        binding.buttonStartWalking.setOnClickListener {
            val intent = Intent(this, activity_watching_map::class.java)
            val weatherText = skyToText(cachedWeather?.sky)
            intent.putExtra("weather", weatherText)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }

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
        Log.d(TAG, "baseDate=$baseDate, baseTime=$baseTime")

        if (cachedWeather != null && cachedWeather?.baseDate == baseDate && cachedWeather?.baseTime == baseTime) {
            Log.d(TAG, "캐시된 날씨 사용됨")
            displayCachedWeather()
        } else {
            Log.d(TAG, "새로 날씨 로딩 시도")
            loadWeather(baseDate, baseTime)
        }

        binding.weatherSection.textRefresh.setOnClickListener {
            val (refreshDate, refreshTime) = getBaseDateTime()
            Log.d(TAG, "날씨 새로고침 요청: $refreshDate / $refreshTime")
            loadWeather(refreshDate, refreshTime)
        }
    }

    private fun loadWeather(baseDate: String, baseTime: String) {
        Log.d(TAG, "loadWeather 호출됨 (baseDate=$baseDate, baseTime=$baseTime)")

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "위치 권한 없음 → 요청")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            return
        }

        binding.weatherSection.root.visibility = View.GONE
        binding.weatherLoadingLayout.root.visibility = View.VISIBLE
        Log.d(TAG, "날씨 UI: 로딩 화면 표시 시작")

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            numUpdates = 1
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location == null) {
                    Log.e(TAG, "위치 정보가 null")
                    return
                }

                Log.d(TAG, "위치 수신 성공: (${location.latitude}, ${location.longitude})")

                val geocoder = Geocoder(this@WalkingStartPageActivity, Locale.KOREAN)
                val finalRegion = try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    addresses?.firstOrNull()?.let { address ->
                        address.subLocality ?: address.thoroughfare ?: address.locality ?: address.adminArea ?: "알 수 없음"
                    } ?: "알 수 없음"
                } catch (e: Exception) {
                    Log.e(TAG, "Geocoder 실패: ${e.message}")
                    "알 수 없음"
                }

                val grid = GpsConverter.toGrid(location.latitude, location.longitude)
                Log.d(TAG, "격자 좌표 변환 완료: (${grid.x}, ${grid.y})")

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
                        Log.d(TAG, "날씨 응답 수신 완료 (성공=${response.isSuccessful})")
                        if (response.isSuccessful) {
                            val items = response.body()?.response?.body?.items?.item
                            val temp = items?.find { it.category == "TMP" }?.fcstValue ?: "-"
                            val sky = items?.find { it.category == "SKY" }?.fcstValue ?: "-"
                            val pop = items?.find { it.category == "POP" }?.fcstValue ?: "-"
                            val tmx = items?.find { it.category == "TMX" }?.fcstValue ?: "-"
                            val tmn = items?.find { it.category == "TMN" }?.fcstValue ?: "-"

                            Log.d(TAG, "날씨 파싱 완료 TMP=$temp SKY=$sky POP=$pop TMX=$tmx TMN=$tmn")

                            cachedWeather = WeatherCache(
                                region = finalRegion,
                                baseDate = baseDate,
                                baseTime = baseTime,
                                temp = temp,
                                sky = sky,
                                pop = pop,
                                tmx = tmx,
                                tmn = tmn
                            )

                            displayCachedWeather()
                        } else {
                            Log.e(TAG, "날씨 응답 실패: code=${response.code()}, body=${response.errorBody()?.string()}")
                            binding.weatherLoadingLayout.root.visibility = View.GONE
                            binding.weatherSection.root.visibility = View.GONE
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.example.petsi.model.WeatherResponse>,
                        t: Throwable
                    ) {
                        Log.e(TAG, "날씨 API 호출 실패: ${t.message}")
                        binding.weatherLoadingLayout.root.visibility = View.GONE
                        binding.weatherSection.root.visibility = View.GONE
                    }
                })
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        Log.d(TAG, "위치 요청 전송 완료")
    }

    private fun displayCachedWeather() {
        val data = cachedWeather ?: return
        Log.d(TAG, "displayCachedWeather 호출: region=${data.region}")
        binding.weatherSection.textRegion.text = "현재 지역 : ${data.region}"
        binding.weatherSection.textTemp.text = "현재 온도 : ${data.temp}°C"
        binding.weatherSection.textSky.text = "날씨 : ${skyToText(data.sky)}"
        binding.weatherSection.textTmx.text = "최고 : ${data.tmx}°C"
        binding.weatherSection.textTmn.text = "최저 : ${data.tmn}°C"
        binding.weatherSection.textPop.text = "강수 확률 : ${data.pop}%"
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
