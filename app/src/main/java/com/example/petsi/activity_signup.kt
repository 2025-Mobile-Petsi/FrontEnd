package com.example.petsi

import android.app.AlertDialog
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
    private lateinit var btnSignIn: ImageButton

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
                    startActivity(Intent(this, activity_watching_map::class.java))
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                    true
                }
                else -> false
            }
        }

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
        btnSignIn = findViewById(R.id.btn_sign_in)

        btnVerify.isEnabled = false
        btnVerifyCode.isEnabled = false
        btnCheckId.isEnabled = false
        btnRegister.isEnabled = false

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateButtonStates()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etId.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)
        etName.addTextChangedListener(watcher)
        etPhone.addTextChangedListener(watcher)
        etVerificationCode.addTextChangedListener(watcher)

        btnCheckId.setOnClickListener {
            val id = etId.text.toString().trim()
            if (id.isNotBlank()) {
                checkIdDuplicate(id)
            }
        }

        btnVerify.setOnClickListener {
            val phone = etPhone.text.toString().trim().replace("-", "")
            if (phone.length == 11) {
                sendVerificationCode(phone)
            }
        }

        btnVerifyCode.setOnClickListener {
            val phone = etPhone.text.toString().trim().replace("-", "")
            val code = etVerificationCode.text.toString().trim()
            if (phone.isNotBlank() && code.length == 6) {
                verifyCode(phone, code)
            }
        }

        btnRegister.setOnClickListener {
            if (!isAllInputValid()) return@setOnClickListener
            if (!idAvailable || !phoneVerified) return@setOnClickListener
            signup()
        }

        btnSignIn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("알림")
                .setMessage("회원가입에서 나가시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    val intent = Intent(this, activitysignin::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_fast)
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun updateButtonStates() {
        val id = etId.text.toString().trim()
        val pw = etPassword.text.toString().trim()
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim().replace("-", "")
        val code = etVerificationCode.text.toString().trim()

        btnCheckId.isEnabled = id.length >= 5
        btnVerify.isEnabled = phone.length == 11
        btnVerifyCode.isEnabled = code.length == 6

        val isValid = id.isNotBlank() && pw.isNotBlank() && name.isNotBlank() && phone.isNotBlank() && code.isNotBlank() && idAvailable && phoneVerified
        btnRegister.isEnabled = isValid
        btnRegister.setBackgroundColor(getColor(if (isValid) R.color.main_color else R.color.sign_button))
    }

    private fun isAllInputValid(): Boolean {
        return etId.text.isNotBlank() &&
                etPassword.text.isNotBlank() &&
                etName.text.isNotBlank() &&
                etPhone.text.isNotBlank() &&
                etVerificationCode.text.isNotBlank()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        Log.d("SignUpLog", msg)
    }

    private fun checkIdDuplicate(id: String) {
        val request = CheckEmailRequest(id)
        SignApiClient.authApiService.checkIdDuplicate(request)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        val isDuplicate = response.body() == true
                        idAvailable = !isDuplicate
                        val message = if (isDuplicate) "이미 사용 중인 아이디입니다." else "사용 가능한 아이디입니다."
                        toast(message)
                        updateButtonStates()
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
                        updateButtonStates()
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