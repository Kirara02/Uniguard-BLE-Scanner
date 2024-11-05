package com.uniguard.ble_scanner.core.data.repositories

import com.uniguard.ble_scanner.core.data.datasource.remote.APIService
import com.uniguard.ble_scanner.core.data.models.BLERequest
import com.uniguard.ble_scanner.core.domain.repositories.BLERepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class BLERepositoryImpl @Inject constructor(
    private val apiService: APIService
) : BLERepository {
    override suspend fun uploads(request: BLERequest): Flow<String> {
        return flowOf(apiService.uploads(request))
    }
}