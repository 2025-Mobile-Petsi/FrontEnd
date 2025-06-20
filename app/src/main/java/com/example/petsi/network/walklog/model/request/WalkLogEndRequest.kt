package com.example.petsi.network.walklog.model.request

import java.time.LocalDateTime

data class WalkLogEndRequest(
    val endTime: String,
    val distance: Double,
    val weather: String
)
