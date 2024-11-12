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

        val cleanedUrl = storedUrl?.replace(Regex("^https?://"), "") ?: originalRequest.url.host

        val scheme = if (Regex("^(\\d{1,3}\\.){3}\\d{1,3}(:\\d+)?$").matches(cleanedUrl)) {
            "http"
        } else {
            "https"
        }

        val (host, port) = if (cleanedUrl.contains(":")) {
            val splitUrl = cleanedUrl.split(":")
            splitUrl[0] to splitUrl[1].toInt()
        } else {
            cleanedUrl to -1
        }

        val newUrlBuilder = originalRequest.url.newBuilder()
            .scheme(scheme)
            .host(host)
            .encodedPath(originalRequest.url.encodedPath)

        if (port != -1) {
            newUrlBuilder.port(port)
        }

        val newUrl = newUrlBuilder.build()

        val username = "Gate"
        val password = "rYn5gqJ4X0ZDdvyPEut2Uhs1FBwVxbGI"
        val basicAuth = Credentials.basic(username, password)

        // Buat request baru dengan URL baru dan header tambahan
        val request = originalRequest.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", basicAuth)
            .url(newUrl)
            .build()

        return chain.proceed(request)
    }
}

