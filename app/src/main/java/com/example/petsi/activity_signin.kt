package com.example.petsi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petsi.sign.SignApiClient
import com.example.petsi.sign.model.request.LoginRequest
import com.example.petsi.sign.model.response.LoginResponse
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

        // 뷰 연결
        etId = findViewById(R.id.et_id)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnSignUp = findViewById(R.id.btn_sign_up)

        // 회원가입 버튼
        btnSignUp.setOnClickListener {
            val intent = Intent(this, activitysignup::class.java)
            startActivity(intent)
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
                        Toast.makeText(this@activitysignin, "${user?.username}님 환영합니다!", Toast.LENGTH_SHORT).show()
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
