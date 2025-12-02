package com.example.eco_plate.data.api

import com.example.eco_plate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    // Customer order endpoints
    @GET("api/orders")
    suspend fun getMyOrders(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<OrdersResponse>>

    @GET("api/orders/{id}")
    suspend fun getOrder(@Path("id") orderId: String): Response<ApiResponse<Order>>

    // Store owner order endpoints
    @GET("api/store/orders")
    suspend fun getStoreOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<OrdersResponse>>

    @GET("api/store/orders/{id}")
    suspend fun getStoreOrder(@Path("id") orderId: String): Response<ApiResponse<Order>>

    @PUT("api/store/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: String,
        @Body request: UpdateOrderStatusRequest
    ): Response<ApiResponse<Order>>

    @POST("api/store/orders/{id}/refund")
    suspend fun refundOrder(
        @Path("id") orderId: String,
        @Body request: RefundRequest
    ): Response<ApiResponse<RefundResponse>>
}
