package com.example.petsi.network

data class NaverSearchResponse(
    val items: List<NaverPlaceItem>
)

data class NaverPlaceItem(
    val title: String,
    val address: String,
    val mapx: String,
    val mapy: String
)