// ✅ VerifyCodeRequest.kt
package com.example.petsi.sign.model.request

data class VerifyCodeRequest(
    val phoneNumber: String,
    val code: String
)
