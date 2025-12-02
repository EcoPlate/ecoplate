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
    suspend fun getMyOrders(): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading())
        try {
            val response = orderApi.getMyOrders()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to fetch orders"))
                }
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
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to fetch order details"))
                }
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

    suspend fun updateOrderStatus(orderId: String, status: String, notes: String? = null): Flow<Resource<Order>> = flow {
        emit(Resource.Loading())
        try {
            val request = UpdateOrderStatusRequest(status, notes)
            val response = orderApi.updateOrderStatus(orderId, request)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to update order status"))
                }
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

    suspend fun getStoreOrders(): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading())
        try {
            val response = orderApi.getStoreOrders()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to fetch store orders"))
                }
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
