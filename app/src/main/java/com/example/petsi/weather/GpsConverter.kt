package com.example.petsi.weather

data class GridXY(val x: Int, val y: Int)

object GpsConverter {
    fun toGrid(lat: Double, lon: Double): GridXY {
        val RE = 6371.00877
        val GRID = 5.0
        val SLAT1 = 30.0
        val SLAT2 = 60.0
        val OLON = 126.0
        val OLAT = 38.0
        val XO = 56
        val YO = 122

        val DEGRAD = Math.PI / 180.0
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        val sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        val snLog = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
        val sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        val sfPow = Math.pow(sf, snLog) * Math.cos(slat1) / snLog
        val ro = re * sfPow / Math.pow(Math.tan(Math.PI * 0.25 + olat * 0.5), snLog)

        val ra = re * sfPow / Math.pow(Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5), snLog)
        var theta = lon * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= snLog

        val x = (ra * Math.sin(theta) + XO + 0.5).toInt()
        val y = (ro - ra * Math.cos(theta) + YO + 0.5).toInt()

        return GridXY(x, y)
    }
}
