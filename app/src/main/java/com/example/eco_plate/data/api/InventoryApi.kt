package com.example.eco_plate.data.api

import com.example.eco_plate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface InventoryApi {
    @GET("inventory/stores/{storeId}/items")
    suspend fun getStoreItems(
        @Path("storeId") storeId: String,
        @Query("category") category: String? = null,
        @Query("available") available: Boolean? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20
    ): Response<List<Item>>

    @GET("inventory/items/{id}")
    suspend fun getItem(@Path("id") itemId: String): Response<Item>

    @POST("inventory/items")
    suspend fun createItem(@Body item: @JvmSuppressWildcards Map<String, Any>): Response<Item>

    @PATCH("inventory/items/{id}")
    suspend fun updateItem(
        @Path("id") itemId: String,
        @Body item: @JvmSuppressWildcards Map<String, Any>
    ): Response<Item>

    @DELETE("inventory/items/{id}")
    suspend fun deleteItem(@Path("id") itemId: String): Response<ApiResponse<Nothing>>

    @PATCH("inventory/items/{id}/availability")
    suspend fun updateItemAvailability(
        @Path("id") itemId: String,
        @Body availability: Map<String, Boolean>
    ): Response<Item>

    @PATCH("inventory/items/{id}/quantity")
    suspend fun updateItemQuantity(
        @Path("id") itemId: String,
        @Body quantity: Map<String, Int>
    ): Response<Item>
}
