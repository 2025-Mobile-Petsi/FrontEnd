package com.example.petsi

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WalkResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.section_walking_information_result)

        val startTime = intent.getStringExtra("startTime")
        val endTime = intent.getStringExtra("endTime")
        val totalTime = intent.getStringExtra("totalTime")
        val totalDistance = intent.getStringExtra("totalDistance")

        findViewById<TextView>(R.id.startTime).text = startTime
        findViewById<TextView>(R.id.endTime).text = endTime
        findViewById<TextView>(R.id.totalTime).text = totalTime
        findViewById<TextView>(R.id.totalDistance).text = totalDistance
    }
}
