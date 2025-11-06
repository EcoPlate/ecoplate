package com.example.eco_plate.data.api

import com.example.eco_plate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<Order>

    @GET("orders")
    suspend fun getMyOrders(): Response<List<Order>>

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") orderId: String): Response<Order>

    @PATCH("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: String,
        @Body request: UpdateOrderStatusRequest
    ): Response<Order>

    @GET("orders/stores/{storeId}")
    suspend fun getStoreOrders(@Path("storeId") storeId: String): Response<List<Order>>
}
