package model.request

data class VerifyCodeRequest (
   val phoneNumber: String,
   val code: String
)