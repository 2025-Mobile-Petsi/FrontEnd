package com.example.petsi.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverSearchService {
    @GET("v1/search/local.json")
    fun searchPlace(
        @Query("query") query: String,
        @Query("display") display: Int = 5
    ): Call<NaverSearchResponse>
}