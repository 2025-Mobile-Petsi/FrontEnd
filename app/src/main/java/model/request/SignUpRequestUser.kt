package model.request

data class ResponseUser (
    val email: String,
    val password: String,
    val username: String,
    val phoneNumber: String
)