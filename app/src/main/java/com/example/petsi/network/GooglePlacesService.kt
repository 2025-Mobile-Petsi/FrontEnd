package com.example.petsi.network

import com.example.petsi.network.GooglePlacesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesService {
    @GET("maps/api/place/textsearch/json")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("key") apiKey: String,
        @Query("language") language: String
    ): Response<GooglePlacesResponse>
}
