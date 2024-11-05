package com.uniguard.ble_scanner.core.domain.usecases

abstract class BaseUseCase <in Params, out T>  {
     abstract suspend fun execute(params: Params) : T
}