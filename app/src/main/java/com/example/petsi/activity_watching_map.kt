package com.example.petsi

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petsi.network.GooglePlacesClient
import com.example.petsi.network.PlaceResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class activity_watching_map : AppCompatActivity(), OnMapReadyCallback {

    private var naverMap: NaverMap? = null
    private lateinit var locationSource: FusedLocationSource
    private val markerList = mutableListOf<Marker>()
    private val apiKey = "AIzaSyBJQf641ng07ZFAANR894VKImePUfvA04I"

    private lateinit var etSearch: EditText
    private lateinit var btnSearch: ImageButton
    private lateinit var btnMyLocation: View
    private lateinit var placeCardView: View
    private lateinit var tvPlaceName: TextView
    private lateinit var tvAddress: TextView

    private var selectedCategoryButton: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watching_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_view) as? MapFragment
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().replace(R.id.map_view, it).commit()
            }
        mapFragment.getMapAsync(this)

        locationSource = FusedLocationSource(this, 1000)

        etSearch = findViewById(R.id.et_search)
        btnSearch = findViewById(R.id.btn_search)
        btnMyLocation = findViewById(R.id.btnMyLocation)
        placeCardView = findViewById(R.id.placeCardView)
        tvPlaceName = findViewById(R.id.tvPlaceName)
        tvAddress = findViewById(R.id.tvAddress)

        val btnCafe = findViewById<LinearLayout>(R.id.btn_cafe)
        val btnFood = findViewById<LinearLayout>(R.id.btn_food)
        val btnVet = findViewById<LinearLayout>(R.id.btn_vet)
        val btnPark = findViewById<LinearLayout>(R.id.btn_park)

        btnCafe.setOnClickListener {
            updateSelectedCategory(btnCafe)
            searchMultipleKeywords(listOf("카페", "커피", "coffee", "다방"), "cafe")
        }
        btnFood.setOnClickListener {
            updateSelectedCategory(btnFood)
            searchPlaces("음식점", "food")
        }
        btnVet.setOnClickListener {
            updateSelectedCategory(btnVet)
            searchPlaces("동물병원", "vet")
        }
        btnPark.setOnClickListener {
            updateSelectedCategory(btnPark)
            searchPlaces("공원", "park")
        }

        btnSearch.setOnClickListener {
            val keyword = etSearch.text.toString()
            if (keyword.isNotBlank()) {
                selectedCategoryButton?.isSelected = false
                selectedCategoryButton = null
                searchPlaces(keyword, "default")
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.nav_map
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    true
                }
                R.id.nav_walk -> {
                    startActivity(Intent(this, WalkingStartPageActivity::class.java))
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    true
                }
                R.id.nav_map -> true
                else -> false
            }
        }

        btnMyLocation.setOnClickListener {
            val coord = naverMap?.locationOverlay?.position
            coord?.let {
                naverMap?.moveCamera(CameraUpdate.scrollTo(coord))
                Log.d("MyLocation", "내 위치 이동: $coord")
            }
        }

        placeCardView.setOnClickListener {
            hidePlaceCard()
        }

        findViewById<View>(R.id.activity_watching_map).setOnClickListener {
            if (placeCardView.visibility == View.VISIBLE) {
                hidePlaceCard()
            }
        }
    }

    private fun updateSelectedCategory(newSelected: LinearLayout) {
        selectedCategoryButton?.isSelected = false
        newSelected.isSelected = true
        selectedCategoryButton = newSelected
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        map.locationSource = locationSource
        map.locationTrackingMode = LocationTrackingMode.Face  // ✅ 또는 FollowNoHeading

        map.locationOverlay.apply {
            isVisible = true
            icon = OverlayImage.fromResource(R.color.main_color)
        }

        Log.d("MapReady", "위치 추적 모드: Face / 기본 마커 적용됨")
    }

    private fun hidePlaceCard() {
        placeCardView.clearAnimation()
        placeCardView.animate()
            .translationY(placeCardView.height.toFloat() + 200f)
            .setDuration(300)
            .withEndAction {
                placeCardView.visibility = View.GONE
                placeCardView.translationY = 0f
            }
            .start()
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
                    apiKey = apiKey,
                    language = "ko"
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
                                width = 64
                                height = 64
                                map = naverMap

                                setOnClickListener {
                                    tvPlaceName.text = place.name
                                    tvAddress.text = place.formatted_address

                                    val slideUp = TranslateAnimation(
                                        0f, 0f,
                                        placeCardView.height.toFloat(), 0f
                                    ).apply {
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
        return OverlayImage.fromResource(R.drawable.ic_filtering_dot)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (::locationSource.isInitialized && locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap?.locationTrackingMode = LocationTrackingMode.None
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
