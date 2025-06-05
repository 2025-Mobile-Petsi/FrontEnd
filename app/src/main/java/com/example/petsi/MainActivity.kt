package com.example.petsi

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ 지도 이미지 클릭 시 activity_walking_with_map으로 이동
        val lostAnimalImageView = findViewById<ImageView>(R.id.lost_animal)
        lostAnimalImageView.setOnClickListener {
            val intent = Intent(this, activity_walking_with_map::class.java)
            startActivity(intent)
        }

        // ✅ 하단 네비게이션 바 "지도" 버튼 클릭 시 이동
        // 현재 화면이 지도이면 그대로 유지
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    val intent = Intent(this, activity_walking_with_map::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
