package com.example.petsi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petsi.sign.SignApiClient
import com.example.petsi.sign.model.request.*
import com.example.petsi.sign.model.response.ResponseUser
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class activitysignup : AppCompatActivity() {

    private lateinit var etId: EditText
    private lateinit var btnCheckId: Button
    private lateinit var etPhone: EditText
    private lateinit var btnVerify: Button
    private lateinit var layoutVerification: LinearLayout
    private lateinit var etVerificationCode: EditText
    private lateinit var btnVerifyCode: Button
    private lateinit var tvTimer: TextView
    private lateinit var etPassword: EditText
    private lateinit var etName: EditText
    private lateinit var btnRegister: Button

    private var idAvailable = false
    private var phoneVerified = false
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
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
        btnCheckId = findViewById(R.id.btn_check_id)
        etPhone = findViewById(R.id.et_phone)
        btnVerify = findViewById(R.id.btn_verify)
        layoutVerification = findViewById(R.id.layout_verification)
        etVerificationCode = findViewById(R.id.et_verification_code)
        btnVerifyCode = findViewById(R.id.btn_verify_code)
        tvTimer = findViewById(R.id.tv_timer)
        etPassword = findViewById(R.id.et_password)
        etName = findViewById(R.id.et_name)
        btnRegister = findViewById(R.id.btn_register)

        // 버튼 초기 상태
        btnVerify.isEnabled = false
        btnVerifyCode.isEnabled = false
        btnCheckId.isEnabled = false

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateRegisterButtonStyle()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etId.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)
        etName.addTextChangedListener(watcher)
        etPhone.addTextChangedListener(watcher)
        etVerificationCode.addTextChangedListener(watcher)

        etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().replace("-", "")
                btnVerify.isEnabled = phone.length == 11
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                btnVerifyCode.isEnabled = s?.length == 6
                updateRegisterButtonStyle()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                btnCheckId.isEnabled = s?.length ?: 0 >= 5
                updateRegisterButtonStyle()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnCheckId.setOnClickListener {
            val id = etId.text.toString().trim()
            if (id.isBlank()) {
                toast("아이디를 입력해주세요.")
                return@setOnClickListener
            }
            checkIdDuplicate(id)
        }

        btnVerify.setOnClickListener {
            val phone = etPhone.text.toString().trim().replace("-", "")
            if (phone.isBlank()) {
                toast("전화번호를 입력해주세요.")
                return@setOnClickListener
            }
            sendVerificationCode(phone)
        }

        btnVerifyCode.setOnClickListener {
            val phone = etPhone.text.toString().trim().replace("-", "")
            val code = etVerificationCode.text.toString().trim()
            if (phone.isBlank() || code.isBlank()) {
                toast("전화번호와 인증번호를 모두 입력해주세요.")
                return@setOnClickListener
            }
            verifyCode(phone, code)
        }

        btnRegister.setOnClickListener {
            when {
                etName.text.isNullOrBlank() -> toast("이름을 입력해주세요.")
                etPhone.text.isNullOrBlank() -> toast("전화번호를 입력해주세요.")
                etVerificationCode.text.isNullOrBlank() -> toast("인증번호를 입력해주세요.")
                etId.text.isNullOrBlank() -> toast("아이디를 입력해주세요.")
                etPassword.text.isNullOrBlank() -> toast("비밀번호를 입력해주세요.")
                !idAvailable -> toast("아이디 중복 확인을 해주세요.")
                !phoneVerified -> toast("전화번호 인증을 해주세요.")
                else -> signup()
            }
        }
    }

    private fun updateRegisterButtonStyle() {
        val isValid = isAllInputValid() && idAvailable && phoneVerified
        btnRegister.setBackgroundColor(getColor(if (isValid) R.color.main_color else R.color.sign_button))
        btnRegister.isEnabled = true  // 항상 활성화 상태
    }

    private fun isAllInputValid(): Boolean {
        return etId.text.isNotBlank() &&
                etPassword.text.isNotBlank() &&
                etName.text.isNotBlank() &&
                etPhone.text.isNotBlank() &&
                etVerificationCode.text.isNotBlank()
    }

    private fun checkIdDuplicate(id: String) {
        val request = CheckEmailRequest(id)
        SignApiClient.authApiService.checkIdDuplicate(request)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        val isDuplicate = response.body() == true
                        idAvailable = !isDuplicate
                        toast(if (isDuplicate) "이미 사용 중인 아이디입니다." else "사용 가능한 아이디입니다.")
                        updateRegisterButtonStyle()
                    } else {
                        toast("서버 오류: 중복확인 실패")
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    toast("네트워크 오류: 중복확인 실패")
                }
            })
    }

    private fun sendVerificationCode(phone: String) {
        val request = PhoneNumberRequest(phone)
        SignApiClient.authApiService.sendVerificationCode(request)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        toast("인증번호가 전송되었습니다.")
                        layoutVerification.visibility = View.VISIBLE
                        val innerLayout = layoutVerification.getChildAt(0) as LinearLayout
                        innerLayout.visibility = View.VISIBLE
                        startTimer()
                    } else {
                        toast("인증번호 전송 실패 (code: ${response.code()})")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    toast("네트워크 오류: 인증번호 전송 실패")
                }
            })
    }

    private fun verifyCode(phone: String, code: String) {
        val request = VerifyCodeRequest(phone, code)
        SignApiClient.authApiService.verifyCode(request)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        toast("인증 성공!")
                        phoneVerified = true
                        tvTimer.text = "인증 완료"
                        tvTimer.setTextColor(Color.BLUE)
                        countDownTimer?.cancel()
                        etVerificationCode.isEnabled = false
                        btnVerifyCode.isEnabled = false
                        updateRegisterButtonStyle()
                    } else {
                        toast("인증번호가 일치하지 않습니다.")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    toast("네트워크 오류: 인증 실패")
                }
            })
    }

    private fun startTimer() {
        tvTimer.visibility = View.VISIBLE
        tvTimer.setTextColor(Color.RED)
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(180000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                tvTimer.text = String.format("%02d:%02d 남음", minutes, seconds)
            }
            override fun onFinish() {
                tvTimer.text = "시간 만료"
            }
        }.start()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        Log.d("SignUpLog", "Toast: $msg")
    }

    private fun signup() {
        val request = SignUpRequestUser(
            email = etId.text.toString().trim(),
            password = etPassword.text.toString().trim(),
            username = etName.text.toString().trim(),
            phoneNumber = etPhone.text.toString().trim().replace("-", "")
        )

        SignApiClient.authApiService.signup(request)
            .enqueue(object : Callback<ResponseUser> {
                override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>) {
                    if (response.isSuccessful) {
                        toast("회원가입이 완료되었습니다!")
                        finish()
                        overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    } else {
                        toast("회원가입 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseUser>, t: Throwable) {
                    toast("네트워크 오류: 회원가입 요청 실패")
                }
            })
    }
}
