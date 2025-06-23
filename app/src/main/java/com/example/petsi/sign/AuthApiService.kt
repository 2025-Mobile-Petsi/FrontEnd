package com.example.petsi.sign

import com.example.petsi.sign.model.request.CheckEmailRequest
import com.example.petsi.sign.model.response.ResponseEmail
import com.example.petsi.sign.model.request.PhoneNumberRequest
import com.example.petsi.sign.model.request.VerifyCodeRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("/api/sms/send-code")
    fun sendVerificationCode(
        @Body request: PhoneNumberRequest
    ): Call<Boolean>

    @POST("/api/sms/verify-code")
    fun verifyCode(
        @Body request: VerifyCodeRequest
    ): Call<Boolean>

    @POST("/api/auth/check-email")
    fun checkIdDuplicate(
        @Body request: CheckEmailRequest
    ): Call<ResponseEmail>  // == Call<Boolean>

}
