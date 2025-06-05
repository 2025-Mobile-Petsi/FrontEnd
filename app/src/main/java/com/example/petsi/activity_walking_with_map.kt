package com.example.petsi

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petsi.databinding.ActivityWalkingWithMapBinding
import com.example.petsi.databinding.WalkingInformationRunningBinding
import com.example.petsi.network.ApiClient
import com.example.petsi.network.NaverSearchResponse
import com.example.petsi.network.NaverPlaceItem
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class activity_walking_with_map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityWalkingWithMapBinding
    private lateinit var walkInfoBinding: WalkingInformationRunningBinding
    private lateinit var naverMap: NaverMap
    private val activeMarkers = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ 바인딩 연결
        binding = ActivityWalkingWithMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ include된 레이아웃 바인딩 수동 연결
        val walkInfoView = findViewById<View>(R.id.layout_walk_summary)
        walkInfoBinding = WalkingInformationRunningBinding.bind(walkInfoView)

        // ✅ 상태바 여백 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.activityWalkingWithMap) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ 지도 초기화
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)

        // ✅ 검색창 검색
        binding.btnSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString()
            if (keyword.isNotBlank()) {
                searchAndShowMarkers(keyword, null)
            }
        }

        // ✅ 카테고리 버튼들
        binding.btnFood.setOnClickListener {
            searchAndShowMarkers("음식점", R.drawable.dogpaw)
        }
        binding.btnCafe.setOnClickListener {
            searchAndShowMarkers("카페", R.drawable.dogpaw)
        }
        binding.btnVet.setOnClickListener {
            searchAndShowMarkers("동물병원", R.drawable.dogpaw)
        }
        binding.btnPark.setOnClickListener {
            searchAndShowMarkers("공원", R.drawable.dogpaw)
        }
    }

    override fun onMapReady(map: NaverMap) {
        this.naverMap = map
    }

    // ✅ 마커 표시 및 클릭 시 카드뷰에 정보 출력
    private fun searchAndShowMarkers(keyword: String, markerResId: Int?) {
        activeMarkers.forEach { it.map = null }
        activeMarkers.clear()

        val service = ApiClient.getNaverSearchService()
        service.searchPlace(keyword).enqueue(object : Callback<NaverSearchResponse> {
            override fun onResponse(call: Call<NaverSearchResponse>, response: Response<NaverSearchResponse>) {
                val items = response.body()?.items ?: return
                Log.d("NAVER_SEARCH", "검색 결과 ${items.size}개")

                for (place in items) {
                    val lat = place.mapy.toDouble() / 1e7
                    val lng = place.mapx.toDouble() / 1e7
                    val title = place.title.replace("<b>", "").replace("</b>", "")
                    val address = place.address

                    val marker = Marker().apply {
                        position = LatLng(lat, lng)
                        captionText = title
                        map = naverMap
                        markerResId?.let {
                            icon = OverlayImage.fromResource(it)
                        }

                        tag = place

                        setOnClickListener {
                            val info = tag as? NaverPlaceItem
                            if (info != null) {
                                val cleanTitle = info.title.replace("<b>", "").replace("</b>", "")
                                binding.tvPlaceName.text = cleanTitle
                                binding.tvAddress.text = info.address
                                binding.placeCardView.visibility = View.VISIBLE

                                // ✅ 기존 산책 정보 요약 레이아웃 숨김
                                walkInfoBinding.root.visibility = View.GONE
                            }
                            true
                        }
                    }

                    activeMarkers.add(marker)
                }
            }

            override fun onFailure(call: Call<NaverSearchResponse>, t: Throwable) {
                Log.e("NAVER_SEARCH", "검색 실패: ${t.message}")
            }
        })
    }
}
