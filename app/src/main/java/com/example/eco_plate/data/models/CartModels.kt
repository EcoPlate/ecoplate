package com.example.eco_plate.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cart(
    val id: String,
    val userId: String?,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val items: List<CartItem> = emptyList()
) : Parcelable

@Parcelize
data class CartItem(
    val id: String = "",
    val cartId: String = "",
    val itemId: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val item: CartItemProduct? = null
) : Parcelable

@Parcelize
data class CartItemProduct(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val images: List<String>? = null,  // Made nullable
    val originalPrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val store: CartItemStore? = null
) : Parcelable

@Parcelize
data class CartItemStore(
    val id: String,
    val name: String,
    val address: String?
) : Parcelable

data class AddToCartRequest(
    val itemId: String,
    val quantity: Int
)

data class UpdateCartItemRequest(
    val quantity: Int
)

data class PaymentIntentRequest(
    val addressId: String? = null,
    val tip: Double = 0.0
)

data class PaymentIntentResponse(
    val clientSecret: String,
    val paymentIntentId: String,
    val amount: Double,
    val cart: Cart
)

data class ConfirmPaymentRequest(
    val paymentIntentId: String,
    val addressId: String? = null,
    val tip: Double = 0.0,
    val customerNotes: String? = null
)

data class ConfirmPaymentResponse(
    val success: Boolean,
    val orders: List<Order>,
    val message: String
)

@Parcelize
data class Order(
    val id: String,
    val orderNumber: String,
    val userId: String,
    val storeId: String,
    val status: String,
    val paymentStatus: String?,
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val deliveryFee: Double,
    val tip: Double,
    val total: Double,
    val isDelivery: Boolean,
    val customerNotes: String?,
    val storeNotes: String?,
    val createdAt: String,
    val items: List<OrderItem>?,
    val store: OrderStore?
) : Parcelable

@Parcelize
data class OrderItem(
    val id: String,
    val orderId: String,
    val itemId: String,
    val quantity: Int,
    val price: Double,
    val total: Double,
    val itemSnapshot: ItemSnapshot?
) : Parcelable

@Parcelize
data class ItemSnapshot(
    val id: String,
    val name: String,
    val description: String?,
    val images: List<String>?,
    val originalPrice: Double,
    val currentPrice: Double
) : Parcelable

@Parcelize
data class OrderStore(
    val id: String,
    val name: String,
    val address: String?,
    val phone: String?
) : Parcelable

data class OrdersResponse(
    val data: List<Order>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

data class UpdateOrderStatusRequest(
    val status: String,
    val storeNotes: String? = null
)

data class RefundRequest(
    val reason: String? = null
)

data class RefundResponse(
    val success: Boolean,
    val refundId: String,
    val order: Order
)

