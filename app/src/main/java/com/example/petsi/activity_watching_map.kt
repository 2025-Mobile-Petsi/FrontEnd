package com.example.petsi

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
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
    private val markerList = mutableListOf<Marker>()
    private val apiKey = "AIzaSyBJQf641ng07ZFAANR894VKImePUfvA04I"

    // UI 요소
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: ImageButton
    private lateinit var btnMyLocation: View
    private lateinit var placeCardView: View
    private lateinit var tvPlaceName: TextView
    private lateinit var tvAddress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watching_map)

        // 지도 Fragment 설정
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_view) as? MapFragment
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().replace(R.id.map_view, it).commit()
            }
        mapFragment.getMapAsync(this)

        // View 직접 참조
        etSearch = findViewById(R.id.et_search)
        btnSearch = findViewById(R.id.btn_search)
        btnMyLocation = findViewById(R.id.btnMyLocation)
        placeCardView = findViewById(R.id.placeCardView)
        tvPlaceName = findViewById(R.id.tvPlaceName)
        tvAddress = findViewById(R.id.tvAddress)

        // 카테고리 버튼
        val btnCafe = findViewById<LinearLayout>(R.id.btn_cafe)
        val btnFood = findViewById<LinearLayout>(R.id.btn_food)
        val btnVet = findViewById<LinearLayout>(R.id.btn_vet)
        val btnPark = findViewById<LinearLayout>(R.id.btn_park)

        btnCafe.setOnClickListener {
            searchMultipleKeywords(listOf("카페", "커피", "coffee", "다방"), "cafe")
        }
        btnFood.setOnClickListener {
            searchPlaces("음식점", "food")
        }
        btnVet.setOnClickListener {
            searchPlaces("동물병원", "vet")
        }
        btnPark.setOnClickListener {
            searchPlaces("공원", "park")
        }
        btnSearch.setOnClickListener {
            val keyword = etSearch.text.toString()
            if (keyword.isNotBlank()) {
                searchPlaces(keyword, "default")
            }
        }

        btnMyLocation.setOnClickListener {
            val jeongwang = LatLng(37.3514, 126.7426)
            naverMap?.moveCamera(CameraUpdate.scrollTo(jeongwang))
        }

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

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        map.moveCamera(CameraUpdate.scrollTo(LatLng(37.3797, 126.8028)))
    }

    private fun searchMultipleKeywords(keywords: List<String>, category: String) {
        runOnUiThread {
            markerList.forEach { it.map = null }
            markerList.clear()
        }
        keywords.forEach { keyword ->
            searchPlaces(keyword, category, append = true)
        }
    }

    private fun searchPlaces(keyword: String, category: String, append: Boolean = false) {
        val location = "37.3797,126.8028"
        val radius = 6000

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
                    val filteredResults = allResults.filterNot {
                        it.name.contains("bbq", ignoreCase = true)
                    }

                    Log.d("GOOGLE_API", "[$keyword] 결과 ${filteredResults.size}개")

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

                                setOnClickListener {
                                    tvPlaceName.text = place.name
                                    tvAddress.text = place.formatted_address

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
                        }
                    }
                } else {
                    Log.e("GOOGLE_API", "API 응답 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("GOOGLE_API", "예외: ${e.message}")
            }
        }
    }

    private fun getIconForCategory(category: String): OverlayImage {
        return when (category) {
            "cafe" -> OverlayImage.fromResource(R.drawable.ic_cafe)
            "food" -> OverlayImage.fromResource(R.drawable.ic_food)
            "vet" -> OverlayImage.fromResource(R.drawable.ic_vet)
            "park" -> OverlayImage.fromResource(R.drawable.ic_park)
            else -> OverlayImage.fromResource(R.drawable.ic_parking)
        }
    }
}
