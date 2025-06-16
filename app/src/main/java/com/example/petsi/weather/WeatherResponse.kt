package com.example.petsi.model

data class WeatherResponse(
    val response: ResponseBody
)

data class ResponseBody(
    val header: ResponseHeader,
    val body: ResponseBodyContent
)

data class ResponseHeader(
    val resultCode: String,
    val resultMsg: String
)

data class ResponseBodyContent(
    val dataType: String,
    val items: WeatherItems,
    val pageNo: Int,
    val numOfRows: Int,
    val totalCount: Int
)

data class WeatherItems(
    val item: List<WeatherItem>
)

data class WeatherItem(
    val baseDate: String,
    val baseTime: String,
    val category: String,
    val fcstDate: String,
    val fcstTime: String,
    val fcstValue: String,
    val nx: Int,
    val ny: Int
)
