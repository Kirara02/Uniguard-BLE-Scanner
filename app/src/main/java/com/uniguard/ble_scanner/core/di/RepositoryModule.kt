package com.uniguard.ble_scanner.core.di

import com.uniguard.ble_scanner.core.data.datasource.remote.APIService
import com.uniguard.ble_scanner.core.data.repositories.BLERepositoryImpl
import com.uniguard.ble_scanner.core.domain.repositories.BLERepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideBLERepository(apiService: APIService) : BLERepository {
        return BLERepositoryImpl(apiService)
    }
}