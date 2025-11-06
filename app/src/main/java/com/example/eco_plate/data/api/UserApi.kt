package com.example.eco_plate.data.api

import com.example.eco_plate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("users/profile")
    suspend fun getProfile(): Response<ApiResponse<User>>

    @PATCH("users/profile")
    suspend fun updateProfile(@Body user: Map<String, Any>): Response<User>

    @GET("users/addresses")
    suspend fun getAddresses(): Response<List<Address>>

    @POST("users/addresses")
    suspend fun createAddress(@Body address: Map<String, Any>): Response<Address>

    @PATCH("users/addresses/{id}")
    suspend fun updateAddress(
        @Path("id") addressId: String,
        @Body address: Map<String, Any>
    ): Response<Address>

    @DELETE("users/addresses/{id}")
    suspend fun deleteAddress(@Path("id") addressId: String): Response<ApiResponse<Nothing>>

    @PATCH("users/addresses/{id}/default")
    suspend fun setDefaultAddress(@Path("id") addressId: String): Response<Address>
}
