package com.example.petsi

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.petsi.network.ApiClient
import com.example.petsi.network.walklog.model.request.WalkLogEndRequest
import com.example.petsi.network.walklog.model.request.WalkLogStartRequest
import com.example.petsi.network.walklog.model.response.WalkLogResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class activity_walking_with_map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val pathOverlay = PathOverlay()

    private var isWalking = false
    private var startTime: Long = 0
    private var accumulatedDistance = 0.0
    private var lastLocation: Location? = null
    private val pathCoords = mutableListOf<LatLng>()
    private var walkLogId: Long = -1L
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            val duration = (System.currentTimeMillis() - startTime) / 1000 / 60
            findViewById<TextView>(R.id.durationTime).text = "${duration}분"
            findViewById<TextView>(R.id.distance).text = String.format("%.1f km", accumulatedDistance / 1000.0)
            handler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walking_with_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)

        locationSource = FusedLocationSource(this, 1000)

        findViewById<ImageView>(R.id.btn_gps).setOnClickListener {
            if (::naverMap.isInitialized) {
                val coord = naverMap.locationOverlay.position
                Log.d("LocationDebug", "GPS 버튼 클릭 위치: $coord")
                naverMap.moveCamera(CameraUpdate.scrollTo(coord))
            }
        }

        findViewById<ImageView>(R.id.logo_home).setOnClickListener {
            confirmExitDuringWalk {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.nav_walk

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    confirmExitDuringWalk {
                        startActivity(Intent(this, MainActivity::class.java))
                        overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    }
                    true
                }
                R.id.nav_walk -> {
                    confirmExitDuringWalk {
                        startActivity(Intent(this, WalkingStartPageActivity::class.java))
                        overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    }
                    true
                }
                R.id.nav_map -> {
                    confirmExitDuringWalk {
                        startActivity(Intent(this, activity_watching_map::class.java))
                        overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    }
                    true
                }
                else -> false
            }
        }

        showRunningLayout()
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Face // ✅ 더 빠른 위치 반응

        naverMap.locationOverlay.apply {
            isVisible = true
            icon = OverlayImage.fromResource(R.color.main_color)
            Log.d("LocationDebug", "기본 마커 설정됨: isVisible=$isVisible, position=$position")
        }

        var firstLocationSet = false
        naverMap.addOnLocationChangeListener { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            naverMap.locationOverlay.position = latLng
            Log.d("LocationDebug", "위치 변경됨: $latLng")

            if (!firstLocationSet) {
                firstLocationSet = true
                Log.d("LocationDebug", "최초 위치로 카메라 이동")
                naverMap.moveCamera(CameraUpdate.scrollTo(latLng).animate(CameraAnimation.Fly))
            }

            if (isWalking) {
                lastLocation?.let {
                    val results = FloatArray(1)
                    Location.distanceBetween(it.latitude, it.longitude, location.latitude, location.longitude, results)
                    accumulatedDistance += results[0]
                }

                lastLocation = location
                pathCoords.add(latLng)

                if (pathCoords.size >= 2) {
                    pathOverlay.coords = pathCoords
                    pathOverlay.map = naverMap
                }
            }
        }
    }

    private fun showRunningLayout() {
        val container = findViewById<FrameLayout>(R.id.walkInfoContainer)
        container.removeAllViews()
        layoutInflater.inflate(R.layout.section_walking_information_running, container, true)

        val btnStartEnd = findViewById<Button>(R.id.btnEndWalk)
        btnStartEnd.text = "산책 시작하기"

        btnStartEnd.setOnClickListener {
            if (!isWalking) {
                AlertDialog.Builder(this)
                    .setMessage("산책을 시작하시겠습니까?")
                    .setPositiveButton("확인") { _, _ ->
                        isWalking = true
                        startTime = System.currentTimeMillis()
                        findViewById<TextView>(R.id.startTime).text =
                            SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(startTime))
                        btnStartEnd.text = "산책 종료하기"
                        accumulatedDistance = 0.0
                        pathCoords.clear()
                        pathOverlay.map = null
                        endMarker?.map = null

                        val currentPosition = naverMap.locationOverlay.position
                        lastLocation = Location("").apply {
                            latitude = currentPosition.latitude
                            longitude = currentPosition.longitude
                        }
                        pathCoords.add(currentPosition)

                        startMarker?.map = null
                        startMarker = Marker().apply {
                            position = currentPosition
                            icon = OverlayImage.fromResource(R.drawable.ic_dot_my)
                            width = 96
                            height = 96
                        }
                        startMarker?.map = naverMap

                        pathOverlay.color = Color.parseColor("#7DB36F")
                        pathOverlay.outlineColor = Color.parseColor("#7DB36F")
                        pathOverlay.width = 15
                        pathOverlay.outlineWidth = 3

                        handler.post(updateRunnable)

                        val request = WalkLogStartRequest(userId = 1L)
                        ApiClient.walkLogApiService.startWalkLog(request).enqueue(object : Callback<WalkLogResponse> {
                            override fun onResponse(call: Call<WalkLogResponse>, response: Response<WalkLogResponse>) {
                                if (response.isSuccessful) {
                                    walkLogId = response.body()?.walkLogId ?: -1L
                                }
                            }

                            override fun onFailure(call: Call<WalkLogResponse>, t: Throwable) {
                                Log.e("WalkStart", "❌ 요청 실패: ${t.message}")
                            }
                        })
                    }
                    .setNegativeButton("취소", null)
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setMessage("산책을 종료하시겠습니까?")
                    .setPositiveButton("확인") { _, _ ->
                        isWalking = false
                        handler.removeCallbacks(updateRunnable)
                        btnStartEnd.text = "산책 시작하기"

                        val endTime = System.currentTimeMillis()
                        val endTimeStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA).format(Date(endTime))
                        val totalDurationMin = (endTime - startTime) / 1000 / 60
                        val formattedStartTime = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(startTime))
                        val formattedEndTime = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(endTime))
                        val formattedDistance = String.format("%.1f km", accumulatedDistance / 1000.0)

                        val currentPosition = naverMap.locationOverlay.position
                        endMarker?.map = null
                        endMarker = Marker().apply {
                            position = currentPosition
                            icon = OverlayImage.fromResource(R.drawable.ic_dot_my)
                            width = 96
                            height = 96
                        }
                        endMarker?.map = naverMap

                        if (walkLogId != -1L) {
                            val reqEnd = WalkLogEndRequest(endTimeStr, accumulatedDistance / 1000.0, "정보 없음")
                            ApiClient.walkLogApiService.endWalkLog(walkLogId, reqEnd).enqueue(object : Callback<WalkLogResponse> {
                                override fun onResponse(c: Call<WalkLogResponse>, r: Response<WalkLogResponse>) {
                                    if (r.isSuccessful) Log.d("WalkEnd", "종료 성공")
                                }

                                override fun onFailure(c: Call<WalkLogResponse>, t: Throwable) {
                                    Log.e("WalkEnd", "요청 오류 ${t.message}")
                                }
                            })
                        }

                        showResultLayout(formattedStartTime, formattedEndTime, "${totalDurationMin}분", formattedDistance)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        }
    }

    private fun showResultLayout(start: String, end: String, total: String, distance: String) {
        val container = findViewById<FrameLayout>(R.id.walkInfoContainer)
        container.removeAllViews()
        layoutInflater.inflate(R.layout.section_walking_information_result, container, true)

        findViewById<TextView>(R.id.startTime)?.text = start
        findViewById<TextView>(R.id.endTime)?.text = end
        findViewById<TextView>(R.id.totalTime)?.text = total
        findViewById<TextView>(R.id.totalDistance)?.text = distance

        findViewById<Button>(R.id.btnBackToMain)?.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("메인으로 이동하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    finish()
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun confirmExitDuringWalk(onConfirmed: () -> Unit) {
        if (isWalking) {
            AlertDialog.Builder(this)
                .setMessage("산책을 종료하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    isWalking = false
                    handler.removeCallbacks(updateRunnable)
                    onConfirmed()
                }
                .setNegativeButton("취소", null)
                .show()
        } else {
            onConfirmed()
        }
    }

    override fun onBackPressed() {
        confirmExitDuringWalk {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
        }
    }
}
