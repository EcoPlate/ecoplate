package com.example.eco_plate.data.repository

import android.util.Log
import com.example.eco_plate.data.api.CartApi
import com.example.eco_plate.data.api.OrderApi
import com.example.eco_plate.data.models.*
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val cartApi: CartApi,
    private val orderApi: OrderApi
) {
    companion object {
        private const val TAG = "PaymentRepository"
    }

    // ==================== Cart Operations ====================

    fun getCart(): Flow<Resource<Cart>> = flow {
        emit(Resource.Loading())
        try {
            val response = cartApi.getCart()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to get cart"))
                }
            } else {
                emit(Resource.Error("Failed to get cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cart", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    fun addToCart(itemId: String, quantity: Int): Flow<Resource<Cart>> = flow {
        emit(Resource.Loading())
        try {
            val response = cartApi.addToCart(AddToCartRequest(itemId, quantity))
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to add to cart"))
                }
            } else {
                emit(Resource.Error("Failed to add to cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to cart", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    fun updateCartItem(itemId: String, quantity: Int): Flow<Resource<Cart>> = flow {
        emit(Resource.Loading())
        try {
            val response = cartApi.updateCartItem(itemId, UpdateCartItemRequest(quantity))
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to update cart"))
                }
            } else {
                emit(Resource.Error("Failed to update cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating cart", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    fun removeFromCart(itemId: String): Flow<Resource<Cart>> = flow {
        emit(Resource.Loading())
        try {
            val response = cartApi.removeFromCart(itemId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to remove from cart"))
                }
            } else {
                emit(Resource.Error("Failed to remove from cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from cart", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    fun clearCart(): Flow<Resource<Cart>> = flow {
        emit(Resource.Loading())
        try {
            val response = cartApi.clearCart()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    // Cart cleared but empty
                    emit(Resource.Success(Cart("", null, 0.0, 0.0, 0.0, 0.0, emptyList())))
                }
            } else {
                emit(Resource.Error("Failed to clear cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cart", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    // ==================== Payment Operations ====================

    fun createPaymentIntent(addressId: String? = null, tip: Double = 0.0): Flow<Resource<PaymentIntentResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = cartApi.createPaymentIntent(PaymentIntentRequest(addressId, tip))
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Log.d(TAG, "Payment intent created: ${apiResponse.data.paymentIntentId}")
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to create payment"))
                }
            } else {
                emit(Resource.Error("Failed to create payment: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating payment intent", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    fun confirmPayment(
        paymentIntentId: String,
        addressId: String? = null,
        tip: Double = 0.0,
        customerNotes: String? = null
    ): Flow<Resource<ConfirmPaymentResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = cartApi.confirmPayment(
                ConfirmPaymentRequest(paymentIntentId, addressId, tip, customerNotes)
            )
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Log.d(TAG, "Payment confirmed: ${apiResponse.data.orders.size} orders created")
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to confirm payment"))
                }
            } else {
                emit(Resource.Error("Failed to confirm payment: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error confirming payment", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    // ==================== Order Operations (Customer) ====================

    fun getMyOrders(page: Int = 1, limit: Int = 20): Flow<Resource<OrdersResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = orderApi.getMyOrders(page, limit)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to get orders"))
                }
            } else {
                emit(Resource.Error("Failed to get orders: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting orders", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    fun getOrder(orderId: String): Flow<Resource<Order>> = flow {
        emit(Resource.Loading())
        try {
            val response = orderApi.getOrder(orderId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to get order"))
                }
            } else {
                emit(Resource.Error("Failed to get order: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting order", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    // ==================== Order Operations (Store Owner) ====================

    fun getStoreOrders(status: String? = null, page: Int = 1, limit: Int = 20): Flow<Resource<OrdersResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = orderApi.getStoreOrders(status, page, limit)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to get store orders"))
                }
            } else {
                emit(Resource.Error("Failed to get store orders: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting store orders", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    fun updateOrderStatus(orderId: String, status: String, notes: String? = null): Flow<Resource<Order>> = flow {
        emit(Resource.Loading())
        try {
            val response = orderApi.updateOrderStatus(orderId, UpdateOrderStatusRequest(status, notes))
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Log.d(TAG, "Order $orderId status updated to $status")
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to update order"))
                }
            } else {
                emit(Resource.Error("Failed to update order: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    fun refundOrder(orderId: String, reason: String? = null): Flow<Resource<RefundResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = orderApi.refundOrder(orderId, RefundRequest(reason))
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Log.d(TAG, "Order $orderId refunded")
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to refund order"))
                }
            } else {
                emit(Resource.Error("Failed to refund order: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refunding order", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
}

