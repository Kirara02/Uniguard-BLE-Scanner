package com.uniguard.ble_scanner.core.network.interceptor

import android.util.Log
import com.uniguard.ble_scanner.core.data.datasource.local.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

internal class HttpRequestInterceptor(
    private val settingsDataStore: SettingsDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val storedUrl = runBlocking {
            settingsDataStore.url.first()
        }

        val domain = storedUrl?.replace(Regex("^https?://"), "") ?: originalRequest.url.host

        Log.d("REQUEST", "Cleaned URL: $domain")

        val endpointPath = originalRequest.url.encodedPath

        val username = "Gate"
        val password = "rYn5gqJ4X0ZDdvyPEut2Uhs1FBwVxbGI"
        val basicAuth = Credentials.basic(username, password)

        val newUrl = originalRequest.url.newBuilder()
            .scheme("https")
            .host(domain)
            .encodedPath(endpointPath)
            .build()

        val request = originalRequest.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", basicAuth)
//            .url(originalRequest.url)
            .url(newUrl)
            .build()

        return chain.proceed(request)
    }
}