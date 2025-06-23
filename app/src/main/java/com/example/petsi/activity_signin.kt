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
import com.google.android.material.bottomnavigation.BottomNavigationView
import model.request.LoginRequest
import model.response.ResponseUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class activitysignin : AppCompatActivity() {

    private lateinit var binding: ActivitySigninBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ 뷰 바인딩 설정
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 버튼 초기 비활성화 + 회색
        updateLoginButtonState(enabled = false)
// ✅ 하단 네비게이션 처리
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.nav_map

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    true
                }
                R.id.nav_walk -> {
                    startActivity(Intent(this, WalkingStartPageActivity::class.java))
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, activity_watching_map ::class.java))
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    true
                }
                else -> false
            }
        }
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

        //로그인 기능
        var login = 0
        binding.btnLogin.setOnClickListener {
            // 요청 데이터 생성: 이메일, 비밀번호
            val request = LoginRequest(
                email = binding.etId.text.toString(),
                password = binding.etPassword.text.toString(),
            )

            api.Login(request).enque(object : Callback<ResponseUser> {
                // 응답 성공 시 호출
                override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>) {
                    if (response.isSuccessful) {
                        Log.d("로그인 정보 전달", "성공: ${response.body()}")  // 정상 응답 로그 출력
                        login = 1
                    } else {
                        Log.e("로그인 정보 전달", "응답 실패: ${response.code()}")  // 서버 응답 실패 (ex. 400, 500)
                    }
                }

                // 통신 자체 실패 (서버 연결 오류, 네트워크 문제 등)
                override fun onFailure(call: Call<ResponseUser>, t: Throwable) {
                    Log.e("로그인", "요청 실패: ${t.message}")
                }


            })

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
