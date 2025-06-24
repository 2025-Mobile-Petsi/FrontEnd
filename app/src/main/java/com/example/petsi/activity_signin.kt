package com.example.petsi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petsi.sign.SignApiClient
import com.example.petsi.sign.model.request.LoginRequest
import com.example.petsi.sign.model.response.LoginResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class activitysignin : AppCompatActivity() {

    private lateinit var etId: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

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

        // 뷰 연결
        etId = findViewById(R.id.et_id)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnSignUp = findViewById(R.id.btn_sign_up)

        // 회원가입 버튼
        btnSignUp.setOnClickListener {
            val intent = Intent(this, activitysignup::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
        }

        // 로그인 버튼
        btnLogin.setOnClickListener {
            val userid = etId.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 🔍 디버깅 로그 추가
            Log.d("LoginDebug", "userid: [$userid], password: [$password]")

            if (userid.isBlank() || password.isBlank()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(userid, password)
        }
    }

    private fun login(email: String, password: String) {
        val request = LoginRequest(email, password)

        SignApiClient.authApiService.login(request)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user != null) {
                            // ✅ userId 저장 (SharedPreferences 사용)
                            val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            prefs.edit().putLong("userId", user.id).apply()

                            // ✅ MainActivity로 이동
                            Toast.makeText(this@activitysignin, "${user.username}님 환영합니다!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@activitysignin, MainActivity::class.java)
                            startActivity(intent)
                            finish() // 로그인 화면 종료
                        } else {
                            Toast.makeText(this@activitysignin, "응답이 비어 있습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("Login", "실패 응답: ${response.errorBody()?.string()}")
                        Toast.makeText(this@activitysignin, "로그인 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@activitysignin, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Login", "오류: ${t.stackTraceToString()}")
                }
            })
    }
}
