package com.uniguard.ble_scanner.core.di

import android.util.Log
import com.google.gson.GsonBuilder
import com.uniguard.ble_scanner.BuildConfig
import com.uniguard.ble_scanner.core.data.datasource.local.SettingsDataStore
import com.uniguard.ble_scanner.core.data.datasource.remote.APIService
import com.uniguard.ble_scanner.core.network.interceptor.HttpRequestInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return if(BuildConfig.DEBUG)HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        else HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(logging: HttpLoggingInterceptor, settingsDataStore: SettingsDataStore): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(HttpRequestInterceptor(settingsDataStore))
            .build()
    }

    @Provides
    @Singleton
    fun provideConverterFactory() : GsonConverterFactory {
        return GsonConverterFactory.create(GsonBuilder().setLenient().create())
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        converterFactory: GsonConverterFactory,
        okHttpClient: OkHttpClient,
    ) : Retrofit {
        val baseUrl = "https://rfidtrack.uniguard.co.id"

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(converterFactory)
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit) : APIService {
        return retrofit.create(APIService::class.java)
    }

}