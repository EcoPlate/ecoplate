package com.example.eco_plate.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class Order(
    val id: String,
    val userId: String,
    val user: User? = null,
    val storeId: String,
    val store: Store? = null,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: OrderStatus,
    val paymentMethod: String? = null,
    val paymentStatus: PaymentStatus,
    val pickupTime: String? = null,
    val orderNumber: String,
    val notes: String? = null,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
) : Parcelable

@Parcelize
data class OrderItem(
    val id: String,
    val orderId: String,
    val itemId: String,
    val item: Item? = null,
    val quantity: Int,
    val price: Double,
    val totalPrice: Double
) : Parcelable

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    PICKED_UP,
    CANCELLED
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}

data class CreateOrderRequest(
    val paymentMethod: String? = "CASH",
    val pickupTime: String? = null,
    val notes: String? = null
)

data class UpdateOrderStatusRequest(
    val status: OrderStatus
)
