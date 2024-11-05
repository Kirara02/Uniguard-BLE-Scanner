package com.uniguard.ble_scanner.core.data.datasource.remote

import com.uniguard.ble_scanner.core.data.models.BLERequest
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {
    @POST("/api/rfid/read")
    suspend fun uploads(@Body request: BLERequest) : String
}