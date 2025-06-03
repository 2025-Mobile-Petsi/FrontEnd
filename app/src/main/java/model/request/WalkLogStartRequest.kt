package model.request

data class WalkLogStartRequest(
    val endTime: String,
    val distance: Double,
    val weather: String
)