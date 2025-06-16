package com.example.petsi.weather

import com.example.petsi.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface KmaApiService {
    @GET("getVilageFcst")
    fun getVillageForecast(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("numOfRows") numOfRows: Int = 1000,
        @Query("pageNo") pageNo: Int = 1,
        @Query("dataType") dataType: String = "JSON",
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): Call<WeatherResponse>
}