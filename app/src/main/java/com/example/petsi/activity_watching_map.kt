package com.example.petsi

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.petsi.databinding.ActivityWatchingMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*

class activity_watching_map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityWatchingMapBinding
    private lateinit var naverMap: NaverMap

    // 기본 위치: 정왕역
    private val defaultLocation = LatLng(37.3514, 126.7425)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatchingMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 지도 프래그먼트 초기화
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: NaverMap) {
        this.naverMap = map

        // 정왕역 위치로 카메라 이동
        map.moveCamera(CameraUpdate.scrollTo(defaultLocation))
        Log.d("DEBUG", "지도 로드됨, 위치: $defaultLocation")
    }
}
