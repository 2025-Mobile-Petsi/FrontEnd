package com.example.petsi

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

        // 초기 버튼 비활성화
        btnVerify.isEnabled = false
        btnVerifyCode.isEnabled = false
        btnCheckId.isEnabled = false

        // 텍스트 변경 감지
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateRegisterButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etId.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)
        etName.addTextChangedListener(watcher)
        etPhone.addTextChangedListener(watcher)
        etVerificationCode.addTextChangedListener(watcher)

        // 전화번호 11자리 → 인증 요청 버튼 활성화
        etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().replace("-", "")
                btnVerify.isEnabled = phone.length == 11
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 인증번호 6자리 → 인증 확인 버튼 활성화
        etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                btnVerifyCode.isEnabled = s?.length == 6
                updateRegisterButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 아이디 5자 이상 → 중복확인 버튼 활성화
        etId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                btnCheckId.isEnabled = s?.length ?: 0 >= 5
                updateRegisterButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 아이디 중복확인
        btnCheckId.setOnClickListener {
            val id = etId.text.toString().trim()
            if (id.isBlank()) {
                toast("아이디를 입력해주세요.")
                return@setOnClickListener
            }
            checkIdDuplicate(id)
        }

        // 인증 요청
        btnVerify.setOnClickListener {
            val phone = etPhone.text.toString().trim().replace("-", "")
            if (phone.isBlank()) {
                toast("전화번호를 입력해주세요.")
                return@setOnClickListener
            }
            sendVerificationCode(phone)
        }

        // 인증 확인
        btnVerifyCode.setOnClickListener {
            val phone = etPhone.text.toString().trim().replace("-", "")
            val code = etVerificationCode.text.toString().trim()
            if (phone.isBlank() || code.isBlank()) {
                toast("전화번호와 인증번호를 모두 입력해주세요.")
                return@setOnClickListener
            }
            verifyCode(phone, code)
        }

        // 회원가입 버튼
        btnRegister.setOnClickListener {
            // 필수 항목 누락 시 항목별 안내
            when {
                etName.text.isNullOrBlank() -> {
                    toast("이름을 입력해주세요.")
                    return@setOnClickListener
                }
                etPhone.text.isNullOrBlank() -> {
                    toast("전화번호를 입력해주세요.")
                    return@setOnClickListener
                }
                etVerificationCode.text.isNullOrBlank() -> {
                    toast("인증번호를 입력해주세요.")
                    return@setOnClickListener
                }
                etId.text.isNullOrBlank() -> {
                    toast("아이디를 입력해주세요.")
                    return@setOnClickListener
                }
                etPassword.text.isNullOrBlank() -> {
                    toast("비밀번호를 입력해주세요.")
                    return@setOnClickListener
                }
            }

            if (!idAvailable) {
                toast("아이디 중복 확인을 해주세요.")
                return@setOnClickListener
            }

            if (!phoneVerified) {
                toast("전화번호 인증을 해주세요.")
                return@setOnClickListener
            }

            signup()
        }
    }

    private fun updateRegisterButtonState() {
        val enabled = isAllInputValid() && idAvailable && phoneVerified
        btnRegister.isEnabled = enabled
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
                        if (isDuplicate) {
                            toast("이미 사용 중인 아이디입니다.")
                            idAvailable = false
                        } else {
                            toast("사용 가능한 아이디입니다.")
                            idAvailable = true
                        }
                        updateRegisterButtonState()
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
                        updateRegisterButtonState()
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
        val email = etId.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val username = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim().replace("-", "")

        val request = SignUpRequestUser(
            email = email,
            password = password,
            username = username,
            phoneNumber = phone
        )

        SignApiClient.authApiService.signup(request)
            .enqueue(object : Callback<ResponseUser> {
                override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>) {
                    if (response.isSuccessful) {
                        toast("회원가입이 완료되었습니다!")
                        finish()
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
