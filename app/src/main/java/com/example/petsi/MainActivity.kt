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

        // 상단 로고 클릭 시 새로고침
        binding.toolbar.logoHome.setOnClickListener {
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
            recreate()
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }

        // 산책하기 버튼 → WalkingStartPageActivity 이동
        binding.mainGoWalking.setOnClickListener {
            val intent = Intent(this, WalkingStartPageActivity::class.java)
            startActivity(intent)
        }

        // 지도 보기 버튼 → activity_watching_map 이동
        binding.btnSeeMap.setOnClickListener {
            val intent = Intent(this, activity_watching_map::class.java)
            startActivity(intent)
        }
    }
}
