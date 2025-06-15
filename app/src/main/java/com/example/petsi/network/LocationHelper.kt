package com.example.petsi.network

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng

object LocationHelper {

    // 테스트용 하드코딩 위치 (경기도 시흥시 중심 좌표)
    private val mockLatLng = LatLng(37.3797, 126.8031)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, onResult: (LatLng?) -> Unit) {
        // 위치 권한 확인
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w("LocationHelper", "위치 권한 없음")
            onResult(null)
            return
        }

        // Google Play services 연결 안됨 → mock 좌표 반환
        Log.w("LocationHelper", "Google Play 미지원 에뮬레이터 - mock 위치 반환")
        onResult(mockLatLng)
    }
}
