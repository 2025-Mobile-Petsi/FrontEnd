package com.example.petsi.network.walklog.service

import com.example.petsi.network.walklog.model.request.WalkLogEndRequest
import com.example.petsi.network.walklog.model.request.WalkLogStartRequest
import com.example.petsi.network.walklog.model.response.WalkLogResponse
import retrofit2.Call
import retrofit2.http.*

// API 인터페이스 구성
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
    @GET("/api/walk-logs/user/{userId}") // @메소드("API url")형식으로 구성
    fun getWalkLogsByUserId( // API 호출 함수 구성
        @Path("userId") userId: Long
    ): Call<List<WalkLogResponse>>
}