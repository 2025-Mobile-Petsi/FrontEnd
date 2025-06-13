package com.example.petsi.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://openapi.naver.com/"

    fun getNaverSearchService(): NaverSearchService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Naver-Client-Id", "auRh0oM8xTWZR50kA87B")
                    .addHeader("X-Naver-Client-Secret", "W5TfsAQ1bd")
                    .build()
                chain.proceed(request)
            }.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(NaverSearchService::class.java)
    }

    private const val WALK_LOG_BASE_URL = "https://api.petsi.com/"  // ← 꼭 실제 서버 주소로 변경

    val walkLogApiService: WalkLogApiService by lazy {
        Retrofit.Builder()
            .baseUrl(WALK_LOG_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WalkLogApiService::class.java)
    }
}