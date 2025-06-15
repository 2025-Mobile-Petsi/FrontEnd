package com.example.petsi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petsi.databinding.ActivityWatchingMapBinding
import com.example.petsi.network.ApiClient
import com.example.petsi.network.NaverSearchResponse
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.*

class activity_watching_map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityWatchingMapBinding
    private lateinit var naverMap: NaverMap
    private val activeMarkers = mutableListOf<Marker>()
    private val fixedCenter: LatLng = LatLng(37.3514, 126.7425) // 정왕역 좌표
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityWatchingMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.activityWatchingMap) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 지도 초기화
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)

        // 내 위치 버튼 (클릭 시 카메라만 이동, 검색 좌표는 고정)
        binding.btnMyLocation.setOnClickListener {
            if (!::naverMap.isInitialized) {
                Log.w("DEBUG", "네이버 맵 초기화 안됨")
                return@setOnClickListener
            }
            naverMap.moveCamera(CameraUpdate.scrollTo(fixedCenter))
            Log.d("DEBUG", "카메라 이동 (정왕역): $fixedCenter")
        }

        // 일반 검색
        binding.btnSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString()
            if (keyword.isNotBlank()) {
                searchAndShowMarkers(keyword, null)
            }
        }

        // 카테고리별 검색
        binding.btnFood.setOnClickListener { searchAndShowMarkers("식당", R.drawable.dogpaw) }
        binding.btnCafe.setOnClickListener { searchAndShowMarkers("커피", R.drawable.dogpaw) }
        binding.btnVet.setOnClickListener { searchAndShowMarkers("동물병원", R.drawable.dogpaw) }
        binding.btnPark.setOnClickListener { searchAndShowMarkers("공원", R.drawable.dogpaw) }
    }

    override fun onMapReady(map: NaverMap) {
        this.naverMap = map
        Log.d("DEBUG", "네이버 지도 준비 완료")

        // 시작 시 정왕역 위치로 이동
        map.moveCamera(CameraUpdate.scrollTo(fixedCenter))
        Log.d("DEBUG", "초기 카메라 위치: $fixedCenter")

        map.addOnCameraIdleListener {
            Log.d("DEBUG", "카메라 멈춤 - 중심 좌표: ${map.cameraPosition.target}")
        }
    }

    private fun searchAndShowMarkers(keyword: String, markerResId: Int?) {
        activeMarkers.forEach { it.map = null }
        activeMarkers.clear()

        val coord = fixedCenter // 시흥 정왕역 좌표로 고정
        Log.d("DEBUG", "검색 좌표 - 위도: ${coord.latitude}, 경도: ${coord.longitude}")

        val service = ApiClient.getNaverSearchService()
        service.searchPlaces(
            query = keyword,
            display = 20, // 많이 받아서 우리가 직접 필터링
            sort = "random", // 어차피 거리 기준 못 씀
            longitude = coord.longitude.toString(),
            latitude = coord.latitude.toString()
        ).enqueue(object : Callback<NaverSearchResponse> {
            override fun onResponse(
                call: Call<NaverSearchResponse>,
                response: Response<NaverSearchResponse>
            ) {
                if (!response.isSuccessful) {
                    Log.e("DEBUG", "응답 실패 코드: ${response.code()}")
                    Log.e("DEBUG", "에러 바디: ${response.errorBody()?.string()}")
                    return
                }

                val items = response.body()?.items
                if (items.isNullOrEmpty()) {
                    Log.d("DEBUG", "검색 결과 없음 또는 파싱 실패")
                    return
                }

                // 🔍 거리 기반 필터링
                val filteredItems = items.filter {
                    try {
                        val lat = it.mapy.toDouble() / 1e7
                        val lng = it.mapx.toDouble() / 1e7
                        val distance = calculateDistance(coord.latitude, coord.longitude, lat, lng)
                        distance < 1000 // 1km 이내만
                    } catch (e: Exception) {
                        false
                    }
                }

                Log.d("DEBUG", "필터링 후 ${filteredItems.size}개")

                for ((index, place) in filteredItems.withIndex()) {
                    try {
                        val lat = place.mapy.toDouble() / 1e7
                        val lng = place.mapx.toDouble() / 1e7
                        Log.d("MARKER", "#$index | ${place.title} | 위도: $lat, 경도: $lng")

                        val marker = Marker().apply {
                            position = LatLng(lat, lng)
                            captionText = place.title.replace("<b>", "").replace("</b>", "")
                            map = naverMap
                            markerResId?.let { icon = OverlayImage.fromResource(it) }
                        }
                        activeMarkers.add(marker)
                    } catch (e: Exception) {
                        Log.e("MARKER", "마커 생성 실패: ${e.message}")
                    }
                }
            }

            override fun onFailure(call: Call<NaverSearchResponse>, t: Throwable) {
                Log.e("NAVER_SEARCH", "검색 실패: ${t.message}")
            }
        })
    }

    // 하버사인 공식으로 거리 계산 (미터 단위)
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0 // 지구 반지름 (m)
        val φ1 = Math.toRadians(lat1)
        val φ2 = Math.toRadians(lat2)
        val Δφ = Math.toRadians(lat2 - lat1)
        val Δλ = Math.toRadians(lon2 - lon1)

        val a = Math.sin(Δφ / 2).pow(2.0) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ / 2).pow(2.0)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}

