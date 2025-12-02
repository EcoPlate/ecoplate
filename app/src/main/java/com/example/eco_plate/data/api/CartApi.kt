package com.example.eco_plate.data.api

import com.example.eco_plate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface CartApi {
    @GET("api/cart")
    suspend fun getCart(): Response<ApiResponse<Cart>>

    @POST("api/cart/items")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<ApiResponse<Cart>>

    @PUT("api/cart/items/{id}")
    suspend fun updateCartItem(
        @Path("id") itemId: String,
        @Body request: UpdateCartItemRequest
    ): Response<ApiResponse<Cart>>

    @DELETE("api/cart/items/{id}")
    suspend fun removeFromCart(@Path("id") itemId: String): Response<ApiResponse<Cart>>

    @DELETE("api/cart")
    suspend fun clearCart(): Response<ApiResponse<Cart>>

    // Payment endpoints
    @GET("api/payments/config")
    suspend fun getPaymentConfig(): Response<ApiResponse<PaymentConfigResponse>>

    @POST("api/payments/create-intent")
    suspend fun createPaymentIntent(@Body request: PaymentIntentRequest): Response<ApiResponse<PaymentIntentResponse>>

    @POST("api/payments/confirm")
    suspend fun confirmPayment(@Body request: ConfirmPaymentRequest): Response<ApiResponse<ConfirmPaymentResponse>>
}

data class PaymentConfigResponse(
    val publishableKey: String
)
