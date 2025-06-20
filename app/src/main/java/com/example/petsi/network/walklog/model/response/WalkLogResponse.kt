package com.example.petsi.network.walklog.model.response

import com.google.gson.annotations.SerializedName

data class WalkLogResponse(
    @SerializedName("id")
    val walkLogId: Long,

    val username: String,

    @SerializedName("startTime")
    val startTime: String,

    val endTime: String,
    val today: String,
    val distance: Double,
    val weather: String
)
