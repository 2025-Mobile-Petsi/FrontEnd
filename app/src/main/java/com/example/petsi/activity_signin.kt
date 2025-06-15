package com.example.petsi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.petsi.databinding.ActivitySigninBinding

class activity_signin : AppCompatActivity() {

    private lateinit var binding: ActivitySigninBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ 뷰 바인딩 설정
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 버튼 초기 비활성화 + 회색
        updateLoginButtonState(enabled = false)

        // ✅ 텍스트 감지: 한 글자라도 입력하면 활성화
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val idFilled = binding.etId.text.toString().isNotEmpty()
                val pwFilled = binding.etPassword.text.toString().isNotEmpty()
                updateLoginButtonState(enabled = idFilled && pwFilled)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.etId.addTextChangedListener(watcher)
        binding.etPassword.addTextChangedListener(watcher)

        // ✅ 회원가입(Sign Up) 버튼 누르면 signup 화면으로 이동
        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this, activity_signup::class.java)
            startActivity(intent)
            finish()
        }
    }

    // ✅ 로그인 버튼 상태 및 색상 업데이트
    private fun updateLoginButtonState(enabled: Boolean) {
        binding.btnLogin.isEnabled = enabled
        val color = if (enabled) "#4CAF50" else "#CCCCCC"
        val colorInt = Color.parseColor(color)
        binding.btnLogin.setBackgroundTintList(ColorStateList.valueOf(colorInt))
    }
}
