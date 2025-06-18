package com.example.petsi.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GooglePlacesClient {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: GooglePlacesService = retrofit.create(GooglePlacesService::class.java)
}
