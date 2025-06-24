package com.example.petsi.sign.model.response

import java.time.LocalDateTime

data class LoginResponse(
    val id: Long,
    val email: String,
    val password: String,
    val username: String,
    val phoneNumber: String,
    val createdAt: String,
    val updatedAt: String

)
