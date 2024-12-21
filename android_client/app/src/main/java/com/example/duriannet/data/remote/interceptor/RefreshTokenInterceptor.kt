/*
package com.example.duriannet.data.remote.api.interceptor

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.duriannet.data.local.prefs.AuthPreferences
import com.example.duriannet.data.remote.api.UserApi
import com.example.duriannet.data.remote.dtos.request.user.RefreshTokenRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject


*/
/**
 *
 * Purpose of [RefreshTokenInterceptor] is to intercept the request and check if the response
 *    is 401 (Unauthorized).
 * If the response is 401, it will try to refresh the access token using the refresh token.
 * If the refresh token is also expired, it will trigger a logout.
 *
 * The lock is used to prevent multiple threads from refreshing the token at the same time.
 * This is important to avoid multiple token refresh requests.
 * If the token is already being refreshed by another thread, the current thread will wait for the
 *    token to be refreshed and then retry the request with the new token.
 * *//*

class RefreshTokenInterceptor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authPreferences: AuthPreferences,
    private val authApiService: UserApi, // Use Provider to avoid circular dependency
) : Interceptor {

    private val lock = ReentrantLock()
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        // First, proceed with the original request
        val request = chain.request()
        var response = chain.proceed(request)


        // Check if response is 401 and we have a refresh token
        if (response.code == 401 && !authPreferences.getRefreshToken().isNullOrBlank()) {
            lock.lock()

            try {
                // Double-check if another thread already refreshed the token
                val currentToken = authPreferences.getAccessToken()
                val requestToken = request.header("Authorization")?.removePrefix("Bearer ")

                // Only refresh if the failed token matches our current token
                if (currentToken == requestToken && !isRefreshing) {
                    isRefreshing = true

                    // Close the previous response before making new requests
                    response.close()

                    val newToken = refreshAccessToken()
                    if (!newToken.isNullOrBlank()) {
                        // Retry the original request with new token
                        val newRequest = request.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                        response = chain.proceed(newRequest)
                    }
                    isRefreshing = false

                } else if (currentToken != requestToken) {
                    // Token has already been refreshed by another thread, retry with current token
                    response.close()
                    val newRequest = request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                    response = chain.proceed(newRequest)
                }

            } catch (e: Exception) {
                // Log the error and handle token refresh failure
                e.printStackTrace()
                handleTokenRefreshFailure()
            } finally {
                lock.unlock() // Release the lock
            }

        }

        return response
    }

    private fun refreshAccessToken(): String? {
        return runBlocking {
            try {
                val refreshToken = authPreferences.getRefreshToken()
                if (refreshToken.isNullOrBlank()) return@runBlocking null

                val response = authApiService.refreshToken(RefreshTokenRequest(refreshToken))

                if (response.isSuccessful) {
                    val newToken = response.body()?.accessToken
                    val newRefreshToken = response.body()?.refreshToken

                    if (!newToken.isNullOrBlank() && !newRefreshToken.isNullOrBlank()) {
                        authPreferences.saveTokens(newToken, newRefreshToken)
                    }

                    newToken
                } else {
                    handleTokenRefreshFailure()
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handleTokenRefreshFailure()
                null
            }
        }
    }

    private fun handleTokenRefreshFailure() {
        // Clear tokens and trigger logout
        authPreferences.clearTokens()

        // Broadcast event to logout
        // This can also be done via LiveData or an EventBus
        val intent = Intent("com.example.duriannet.LOGOUT")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}*/
