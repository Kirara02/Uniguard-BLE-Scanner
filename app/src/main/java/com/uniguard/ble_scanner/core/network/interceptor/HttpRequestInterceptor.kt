package com.uniguard.ble_scanner.core.network.interceptor

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

internal class HttpRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Encode credentials with Basic Authentication
        val username = "Gate"
        val password = "rYn5gqJ4X0ZDdvyPEut2Uhs1FBwVxbGI"
        val basicAuth = Credentials.basic(username, password)

        // Add headers to request
        val request = originalRequest.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", basicAuth)
            .url(originalRequest.url)
            .build()

        return chain.proceed(request)
    }
}