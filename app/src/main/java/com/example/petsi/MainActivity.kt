package com.example.petsi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.petsi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 클릭 시 WalkingStartPageActivity로 이동 + 전환 애니메이션 적용
        binding.mainGoWalking.setOnClickListener {
            val intent = Intent(this, WalkingStartPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }

        // 🔹 지도 보기 버튼 클릭 시
        binding.btnSeeMap.setOnClickListener {
            val intent = Intent(this, MapFilteringActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }

        // ✅ 앱 시작 시 하단 네비게이션 바의 홈 아이콘 선택 상태로 설정
        binding.bottomNav.bottomNavigationView.selectedItemId = R.id.nav_home

        // ✅ 하단 네비게이션 바 클릭 처리
        binding.bottomNav.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // 현재 화면이므로 이동하지 않음

                R.id.nav_walk -> {
                    val intent = Intent(this, WalkingStartPageActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    true
                }

                R.id.nav_map -> {
                    val intent = Intent(this, MapFilteringActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    true
                }

                else -> false
            }
        }
    }
    override fun onResume() {
        super.onResume()
        // 홈 버튼을 항상 선택된 상태로 표시
        binding.bottomNav.bottomNavigationView.selectedItemId = R.id.nav_home
    }
}
