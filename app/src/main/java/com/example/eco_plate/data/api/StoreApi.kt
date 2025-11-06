package com.example.eco_plate.data.api

import com.example.eco_plate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface StoreApi {
    @GET("stores")
    suspend fun getStores(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20,
        @Query("category") category: String? = null
    ): Response<List<Store>>

    @GET("stores/{id}")
    suspend fun getStore(@Path("id") storeId: String): Response<Store>

    @POST("stores")
    suspend fun createStore(@Body store: Map<String, Any>): Response<Store>

    @PATCH("stores/{id}")
    suspend fun updateStore(
        @Path("id") storeId: String,
        @Body store: Map<String, Any>
    ): Response<Store>

    @DELETE("stores/{id}")
    suspend fun deleteStore(@Path("id") storeId: String): Response<ApiResponse<Nothing>>

    @GET("stores/my-stores")
    suspend fun getMyStores(): Response<List<Store>>
}
