package com.example.eco_plate.data.api

import com.example.eco_plate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse>

    @POST("auth/signin")
    suspend fun signIn(@Body request: SignInRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Nothing>>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Nothing>>
}
