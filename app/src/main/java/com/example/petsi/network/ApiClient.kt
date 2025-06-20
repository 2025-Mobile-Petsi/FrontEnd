package com.example.petsi.network

import com.example.petsi.network.walklog.service.WalkLogApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val walkLogApiService: WalkLogApiService by lazy {
        retrofit.create(WalkLogApiService::class.java)
    }
}