package com.example.petsi.sign

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SignApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
}

