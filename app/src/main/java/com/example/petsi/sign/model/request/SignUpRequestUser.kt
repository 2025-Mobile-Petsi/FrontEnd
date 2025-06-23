package com.example.petsi.sign.model.request

data class SignUpRequestUser (
    val email: String,
    val password: String,
    val username: String,
    val phoneNumber: String
)
