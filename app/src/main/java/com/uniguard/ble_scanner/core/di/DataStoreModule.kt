package com.uniguard.ble_scanner.core.di

import android.app.Application
import com.uniguard.ble_scanner.core.data.datasource.local.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(application: Application): SettingsDataStore {
        return SettingsDataStore(application)
    }

}