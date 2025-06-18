package com.example.petsi

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.petsi.network.GooglePlacesClient
import com.example.petsi.network.PlaceResult
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class activity_watching_map : AppCompatActivity(), OnMapReadyCallback {

    private var naverMap: NaverMap? = null
    private val markerList = mutableListOf<Marker>() // 현재 지도에 표시된 마커들 관리
    private val apiKey = "AIzaSyBJQf641ng07ZFAANR894VKImePUfvA04I" // Google Places API 키

    // 카드뷰 뷰 요소
    private lateinit var placeCardView: View
    private lateinit var placeNameTextView: TextView
    private lateinit var addressTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watching_map)

        // 지도 프래그먼트 설정
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_view) as? MapFragment
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().replace(R.id.map_view, it).commit()
            }
        mapFragment.getMapAsync(this)

        // 🔽 카드뷰 관련 View 바인딩
        placeCardView = findViewById(R.id.placeCardView)
        placeNameTextView = findViewById(R.id.tvPlaceName)
        addressTextView = findViewById(R.id.tvAddress)

        // 🔽 필터 버튼 리스너 등록
        findViewById<Button>(R.id.btn_cafe).setOnClickListener {
            searchMultipleKeywords(listOf("카페", "커피", "coffee", "다방"), "cafe")
        }

        findViewById<Button>(R.id.btn_food).setOnClickListener {
            searchPlaces("음식점", "food")
        }

        findViewById<Button>(R.id.btnVet).setOnClickListener {
            searchPlaces("동물병원", "vet")
        }

        findViewById<Button>(R.id.btnPark).setOnClickListener {
            searchPlaces("공원", "park")
        }

        // 🔽 키워드 검색 버튼
        findViewById<Button>(R.id.btn_search).setOnClickListener {
            val keyword = findViewById<EditText>(R.id.et_search).text.toString()
            if (keyword.isNotBlank()) {
                searchPlaces(keyword, "default")
            }
        }

        // 🔽 내 위치 버튼 (정왕동으로 이동)
        findViewById<Button>(R.id.btnMyLocation).setOnClickListener {
            val jeongwang = LatLng(37.3514, 126.7426)
            naverMap?.moveCamera(CameraUpdate.scrollTo(jeongwang))
        }

        // 🔽 카드뷰 클릭 시 슬라이드 다운 애니메이션으로 숨김
        placeCardView.setOnClickListener {
            val slideDown = TranslateAnimation(0f, 0f, 0f, placeCardView.height.toFloat()).apply {
                duration = 300
                fillAfter = true
            }
            slideDown.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    placeCardView.visibility = View.GONE
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
            placeCardView.startAnimation(slideDown)
        }
    }

    // 🔽 지도 준비 완료 시 콜백
    override fun onMapReady(map: NaverMap) {
        naverMap = map
        map.moveCamera(CameraUpdate.scrollTo(LatLng(37.3797, 126.8028))) // 시흥 중심 좌표
    }

    // 🔽 여러 키워드 검색 처리 (ex. "카페", "커피" 등)
    private fun searchMultipleKeywords(keywords: List<String>, category: String) {
        runOnUiThread {
            markerList.forEach { it.map = null }
            markerList.clear()
        }
        keywords.forEach { keyword ->
            searchPlaces(keyword, category, append = true)
        }
    }

    // 🔽 실제 검색 및 마커 표시
    private fun searchPlaces(keyword: String, category: String, append: Boolean = false) {
        val location = "37.3797,126.8028" // 중심 위치 (시흥시청 인근)
        val radius = 6000 // 반경 6km

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = GooglePlacesClient.service.searchPlaces(
                    query = keyword,
                    location = location,
                    radius = radius,
                    apiKey = apiKey
                )

                if (response.isSuccessful) {
                    val allResults: List<PlaceResult> = response.body()?.results ?: emptyList()

                    // 🔽 예외 필터링 (BBQ 제거)
                    val filteredResults = allResults.filterNot {
                        it.name.contains("bbq", ignoreCase = true)
                    }

                    Log.d("GOOGLE_API", "[${keyword}] 결과 ${filteredResults.size}개 (필터링 후)")

                    runOnUiThread {
                        if (!append) {
                            markerList.forEach { it.map = null }
                            markerList.clear()
                        }

                        filteredResults.forEach { place ->
                            val latLng = LatLng(place.geometry.location.lat, place.geometry.location.lng)
                            val marker = Marker().apply {
                                position = latLng
                                captionText = place.name
                                icon = getIconForCategory(category)
                                map = naverMap

                                // 🔽 마커 클릭 시 카드뷰 올라오게 설정
                                setOnClickListener {
                                    placeNameTextView.text = place.name
                                    addressTextView.text = place.formatted_address

                                    val slideUp = TranslateAnimation(0f, 0f, placeCardView.height.toFloat(), 0f).apply {
                                        duration = 300
                                        fillAfter = true
                                    }
                                    placeCardView.visibility = View.VISIBLE
                                    placeCardView.startAnimation(slideUp)
                                    true
                                }
                            }
                            markerList.add(marker)
                            Log.d("GOOGLE_API", "[마커 추가됨] ${place.name}, ${place.formatted_address}")
                        }
                    }
                } else {
                    Log.e("GOOGLE_API", "응답 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("GOOGLE_API", "예외: ${e.message}")
            }
        }
    }

    // 🔽 카테고리에 따른 마커 이미지 선택
    private fun getIconForCategory(category: String): OverlayImage {
        return when (category) {
            "cafe" -> OverlayImage.fromResource(R.drawable.ic_cafe)
            "food" -> OverlayImage.fromResource(R.drawable.ic_food)
            "vet" -> OverlayImage.fromResource(R.drawable.ic_vet)
            "park" -> OverlayImage.fromResource(R.drawable.ic_park)
            else -> OverlayImage.fromResource(R.drawable.ic_parking) // 기본값
        }
    }
}
