package com.example.eco_plate.data.api

import com.example.eco_plate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface CartApi {
    @GET("cart")
    suspend fun getCart(): Response<Cart>

    @POST("cart/items")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<Cart>

    @PATCH("cart/items/{id}")
    suspend fun updateCartItem(
        @Path("id") itemId: String,
        @Body request: UpdateCartItemRequest
    ): Response<Cart>

    @DELETE("cart/items/{id}")
    suspend fun removeFromCart(@Path("id") itemId: String): Response<Cart>

    @DELETE("cart")
    suspend fun clearCart(): Response<ApiResponse<Nothing>>

    @POST("cart/checkout")
    suspend fun checkout(): Response<Order>
}
