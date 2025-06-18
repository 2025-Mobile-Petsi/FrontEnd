package com.example.petsi.network

data class GooglePlacesResponse(
    val results: List<PlaceResult>
)

data class PlaceResult(
    val name: String,
    val formatted_address: String?,
    val geometry: Geometry
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)
