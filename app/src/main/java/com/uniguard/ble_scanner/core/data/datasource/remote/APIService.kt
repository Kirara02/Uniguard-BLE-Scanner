package com.uniguard.ble_scanner.core.data.datasource.remote

import com.uniguard.ble_scanner.core.data.models.BLERequest
import com.uniguard.ble_scanner.core.data.models.BLEResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {
    @POST("/api/rfid/read")
    suspend fun uploads(@Body request: BLERequest) : BLEResponse
}