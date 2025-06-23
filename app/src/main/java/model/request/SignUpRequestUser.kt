package model.request

data class SignUpRequestUser (
    val email: String,//아이디
    val password: String,
    val username: String,
    val phoneNumber: String
)
