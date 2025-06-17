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
import com.example.petsi.network.LocationHelper
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import kotlin.math.*

class activity_walking_with_map : AppCompatActivity(), OnMapReadyCallback {

    private var naverMap: NaverMap? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_walking_with_map)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_walking_with_map)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ 위치 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // ✅ 지도 프래그먼트 연결
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(naverMapInstance: NaverMap) {
        naverMap = naverMapInstance

        // ✅ 현재 위치 요청
        LocationHelper.getCurrentLocation(this) { latLng ->
            latLng?.let { location ->
                naverMap?.let { navMap ->
                    Marker().apply {
                        position = location
                        captionText = "현재 위치"
                        map = navMap
                    }

                    navMap.moveCamera(CameraUpdate.scrollTo(location))
                }
            } ?: Log.e("LOCATION", "위치 정보를 가져올 수 없음")
        }
    }

    // ✅ 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISSION", "위치 권한 허용됨")
            // 권한 허용 시 지도 초기화 다시 시도 가능
        } else {
            Log.e("PERMISSION", "위치 권한 거부됨")
        }
    }
}
