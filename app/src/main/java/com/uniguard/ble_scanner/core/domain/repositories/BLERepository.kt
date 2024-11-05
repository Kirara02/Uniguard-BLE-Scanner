package com.uniguard.ble_scanner.core.domain.repositories

import com.uniguard.ble_scanner.core.data.models.BLERequest
import kotlinx.coroutines.flow.Flow

interface BLERepository {
    suspend fun uploads(request: BLERequest) : Flow<String>
}