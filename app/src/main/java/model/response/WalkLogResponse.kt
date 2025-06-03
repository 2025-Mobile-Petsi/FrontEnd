package model.response

data class WalkLogResponse(
    val walkLogId: Long,
    val username: String,
    val startTime: String,
    val endTime: String,
    val today: String,
    val distance: Double,
    val weather: String
)