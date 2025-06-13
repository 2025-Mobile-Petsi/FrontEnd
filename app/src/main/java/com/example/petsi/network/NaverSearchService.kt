package com.example.petsi.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverSearchService {

    @GET("v1/search/local.json")
    fun searchPlace(
        @Query("query") query: String,
        @Query("coordinate") coordinate: String,  // 예: "126.735,37.345"
        @Query("sort") sort: String = "distance", // 거리순
        @Query("display") display: Int = 5
    ): Call<NaverSearchResponse>
}
