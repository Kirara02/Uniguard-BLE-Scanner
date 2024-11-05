package com.uniguard.ble_scanner.core.domain.usecases.ble

import com.uniguard.ble_scanner.core.data.models.BLERequest
import com.uniguard.ble_scanner.core.domain.repositories.BLERepository
import com.uniguard.ble_scanner.core.domain.usecases.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadsUseCase @Inject constructor(
    private val repository: BLERepository
) : BaseUseCase<BLERequest, Flow<String>>(){
    override suspend fun execute(params: BLERequest): Flow<String> {
        return repository.uploads(params)
    }
}