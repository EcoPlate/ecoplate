package com.example.eco_plate.data.network

import com.example.eco_plate.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            tokenManager.accessToken.first()
        }

        val request = if (!token.isNullOrEmpty()) {
            Log.d("AuthInterceptor", "Adding token to request: Bearer ${token.take(20)}...")
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()
        } else {
            Log.d("AuthInterceptor", "No token available for request to: ${chain.request().url}")
            chain.request()
        }

        return chain.proceed(request)
    }
}
