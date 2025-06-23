package com.example.petsi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.petsi.databinding.ActivityMainBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var startPoint: LatLng? = null
    private var endPoint: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 상단 로고 클릭 시 새로고침
        binding.toolbar.logoHome.setOnClickListener {
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
            recreate()
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }

        // ✅ 프로필 버튼 → 로그인 페이지로 이동
        binding.toolbar.profileButton.setOnClickListener {
            val intent = Intent(this, activitysignin::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }


        // ✅ 산책하기 버튼
        binding.mainGoWalking.setOnClickListener {
            startActivity(Intent(this, WalkingStartPageActivity::class.java))
        }

        // ✅ 지도보기 버튼
        binding.btnSeeMap.setOnClickListener {
            startActivity(Intent(this, activity_watching_map::class.java))
        }

        // ✅ 하단 네비게이션 바 처리
        val navView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // MainActivity → 자신이므로 재시작 대신 무시
                    true
                }
                R.id.nav_walk -> {
                    startActivity(Intent(this, WalkingStartPageActivity::class.java))
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, activity_watching_map::class.java))
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    true
                }
                else -> false
            }
        }
    }
}
