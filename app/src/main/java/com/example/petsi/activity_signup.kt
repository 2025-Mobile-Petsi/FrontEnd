package com.example.petsi

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.petsi.databinding.ActivitySignupBinding
import android.graphics.Color
import android.content.res.ColorStateList

class activity_signup : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 버튼 초기 상태 설정 (회색, 비활성화)
        binding.btnVerify.isEnabled = false
        updateButtonColor(binding.btnVerify, false)
        updateButtonColor(binding.btnVerifyCode, false)
        updateButtonColor(binding.btnCheckId, false)

        // ✅ 전화번호 11자리 입력 시에만 인증 버튼 활성화
        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString()
                val valid = phone.length == 11 && phone.all { it.isDigit() }
                binding.btnVerify.isEnabled = valid
                updateButtonColor(binding.btnVerify, valid)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ 인증번호 입력하면 확인 버튼 활성화
        binding.etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val hasInput = !s.isNullOrBlank()
                binding.btnVerifyCode.isEnabled = hasInput
                updateButtonColor(binding.btnVerifyCode, hasInput)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ 아이디 입력하면 중복확인 버튼 활성화
        binding.etId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val hasInput = !s.isNullOrBlank()
                binding.btnCheckId.isEnabled = hasInput
                updateButtonColor(binding.btnCheckId, hasInput)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ 인증하기 누르면 인증번호 UI 보이고 타이머 시작
        binding.btnVerify.setOnClickListener {
            binding.layoutVerification.visibility = View.VISIBLE
            binding.tvTimer.visibility = View.VISIBLE
            startTimer()
        }

        // ✅ 확인 버튼 누르면 토스트만 표시
        binding.btnVerifyCode.setOnClickListener {
            val code = binding.etVerificationCode.text.toString()
            Toast.makeText(this, "입력한 인증번호: $code", Toast.LENGTH_SHORT).show()
        }

        // ✅ 로그인(Sign In) 버튼 누르면 initial 화면으로 이동
        binding.btnSignIn.setOnClickListener {
            val intent = Intent(this, activitysignin::class.java)
            startActivity(intent)
            finish()
        }
    }

    // ✅ 버튼 색상 바꿔주는 함수
    private fun updateButtonColor(button: View, enabled: Boolean) {
        val color = if (enabled) "#4CAF50" else "#AAAAAA"
        button.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
    }

    // ✅ 타이머 시작 (2분 카운트다운)
    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(2 * 60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                binding.tvTimer.text = "남은 시간: %02d:%02d".format(minutes, seconds)
            }

            override fun onFinish() {
                binding.tvTimer.text = "인증 시간이 만료되었습니다."
            }
        }.start()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
