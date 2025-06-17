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

        // 1. 상단 로고 클릭 시 새로고침 (전환 애니메이션 제거)
        binding.toolbar.logoHome.setOnClickListener {
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)  // 👉 전환 시작 애니메이션
            recreate()
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)  // 👉 recreate 후 적용 애니메이션
        }

        // 2. 산책하기 버튼 → WalkingStartPageActivity
        binding.mainGoWalking.setOnClickListener {
            val intent = Intent(this, WalkingStartPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }

        // 3. 지도 보기 버튼 → MapFilteringActivity
        binding.btnSeeMap.setOnClickListener {
            val intent = Intent(this, activity_watching_map::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }

        // 4. 하단 네비게이션
        binding.bottomNav.bottomNavigationView.selectedItemId = R.id.nav_home
        binding.bottomNav.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

                R.id.nav_walk -> {
                    val intent = Intent(this, WalkingStartPageActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    true
                }

                R.id.nav_map -> {
                    val intent = Intent(this, activity_watching_map::class.java)
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
        binding.bottomNav.bottomNavigationView.selectedItemId = R.id.nav_home
    }
}
