package com.example.petsi.network

import model.request.WalkLogEndRequest
import model.response.WalkLogResponse
import model.request.WalkLogStartRequest

import retrofit2.Call
import retrofit2.http.*

interface WalkLogApiService {

    // 산책 종료
    @PUT("/api/walk-logs/end/{walkLogId}")
    fun endWalkLog(
        @Path("walkLogId") walkLogId: Long,
        @Body request: WalkLogEndRequest
    ): Call<WalkLogResponse>

    // 산책 시작
    @POST("/api/walk-logs/start")
    fun startWalkLog(
        @Body request: WalkLogStartRequest
    ): Call<WalkLogResponse>

    // 특정 유저의 산책 기록 조회
    @GET("/api/walk-logs/user/{userId}")
    fun getWalkLogsByUserId(
        @Path("userId") userId: Long
    ): Call<List<WalkLogResponse>>
}