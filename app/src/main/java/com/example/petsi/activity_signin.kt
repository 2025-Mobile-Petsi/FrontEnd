package com.example.petsi

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.petsi.sign.SignApiClient
import com.example.petsi.sign.model.response.ResponseUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class activitysignin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        val btnSignUp = findViewById<ImageButton>(R.id.btn_sign_up)
        btnSignUp.setOnClickListener {
            val intent = Intent(this, activitysignup::class.java)
            startActivity(intent)
        }
    }
}
