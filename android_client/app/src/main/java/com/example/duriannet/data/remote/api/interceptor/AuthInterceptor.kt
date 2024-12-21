package com.example.duriannet.data.remote.api.interceptor

import android.util.Log
import com.example.duriannet.data.local.prefs.AuthPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 *
 * Purpose of [AuthInterceptor] is to intercept the request and add the Authorization header
 * with the access token if it is available.
 *
 * */

class AuthInterceptor @Inject constructor(
    private val authPreferences: AuthPreferences,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        // Get the access token from the shared preferences
        val accessToken = authPreferences.getAccessToken()

        // Get the original request
        val originalRequest = chain.request()

        // Create a new request builder
        val requestBuilder = originalRequest.newBuilder()

        // Add the Authorization header with the access token if it is available
        accessToken?.let {
            Log.e("AuthInterceptor", "intercept: $accessToken")

            requestBuilder
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        }

        // Build the updated request
        val updatedRequest = requestBuilder.build()

        // Log the updated request headers (for debugging)
        Log.d("AuthInterceptor", "Request Headers:")
        updatedRequest.headers.forEach { header ->
            Log.d("AuthInterceptor", "${header.first}: ${header.second}")
        }

        // Proceed with the updated request
        return chain.proceed(updatedRequest)
    }
}