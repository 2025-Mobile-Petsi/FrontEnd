package com.example.petsi.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverSearchService {
    @GET("v1/search/local.json")
    fun searchPlaces(
        @Query("query") query: String,
        @Query("display") display: Int = 5,
        @Query("sort") sort: String = "random",  // or "distance"
        @Query("x") longitude: String, // ✅ 경도
        @Query("y") latitude: String   // ✅ 위도
    ): Call<NaverSearchResponse>
}