package com.example.eco_plate.data.repository

import com.example.eco_plate.data.api.OrderApi
import com.example.eco_plate.data.models.*
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderApi: OrderApi
) {
    suspend fun createOrder(
        paymentMethod: String? = "CASH",
        pickupTime: String? = null,
        notes: String? = null
    ): Flow<Resource<Order>> = flow {
        emit(Resource.Loading())
        try {
            val request = CreateOrderRequest(paymentMethod, pickupTime, notes)
            val response = orderApi.createOrder(request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to create order"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun getMyOrders(): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading())
        try {
            val response = orderApi.getMyOrders()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch orders"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun getOrder(orderId: String): Flow<Resource<Order>> = flow {
        emit(Resource.Loading())
        try {
            val response = orderApi.getOrder(orderId)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch order details"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Flow<Resource<Order>> = flow {
        emit(Resource.Loading())
        try {
            val request = UpdateOrderStatusRequest(status)
            val response = orderApi.updateOrderStatus(orderId, request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update order status"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun getStoreOrders(storeId: String): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading())
        try {
            val response = orderApi.getStoreOrders(storeId)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch store orders"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}
