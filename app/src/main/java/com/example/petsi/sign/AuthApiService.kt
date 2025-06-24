package com.example.petsi.sign

import com.example.petsi.sign.model.request.CheckEmailRequest
import com.example.petsi.sign.model.request.LoginRequest
import com.example.petsi.sign.model.response.ResponseEmail
import com.example.petsi.sign.model.request.PhoneNumberRequest
import com.example.petsi.sign.model.request.SignUpRequestUser
import com.example.petsi.sign.model.request.VerifyCodeRequest
import com.example.petsi.sign.model.response.LoginResponse
import com.example.petsi.sign.model.response.ResponseUser


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApiService {

    @POST("/api/sms/send-code")
    fun sendVerificationCode(
        @Body request: PhoneNumberRequest
    ): Call<Void>  // ✅ 바꿈

    @POST("/api/sms/verify-code")
    fun verifyCode(
        @Body request: VerifyCodeRequest
    ): Call<Void>  // ✅ 바꿈

    @POST("/api/auth/check-email")
    fun checkIdDuplicate(
        @Body request: CheckEmailRequest
    ): Call<ResponseEmail>  // == Call<Boolean>

    @POST("/api/auth/signup")
    fun signup(
        @Body request: SignUpRequestUser
    ): Call<ResponseUser>

    @Headers("Content-Type: application/json")
    @POST("/api/users/login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>
}
