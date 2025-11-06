package com.example.eco_plate.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class Cart(
    val id: String,
    val userId: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val totalItems: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
) : Parcelable

@Parcelize
data class CartItem(
    val id: String,
    val cartId: String,
    val itemId: String,
    val item: Item? = null,
    val quantity: Int,
    val price: Double,
    val totalPrice: Double,
    @SerializedName("created_at")
    val createdAt: String
) : Parcelable

data class AddToCartRequest(
    val itemId: String,
    val quantity: Int
)

data class UpdateCartItemRequest(
    val quantity: Int
)
