package com.uniguard.ble_scanner.core.di.usecases

import com.uniguard.ble_scanner.core.domain.repositories.BLERepository
import com.uniguard.ble_scanner.core.domain.usecases.ble.UploadsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BLEUseCaseModule {

    @Provides
    @Singleton
    fun provideUploadsUseCase(bleRepository: BLERepository): UploadsUseCase {
        return UploadsUseCase(bleRepository)
    }

}