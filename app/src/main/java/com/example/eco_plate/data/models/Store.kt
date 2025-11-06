package com.example.eco_plate.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class Store(
    val id: String,
    val ownerId: String,
    val name: String,
    val description: String? = null,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val category: StoreCategory,
    val openingHours: Map<String, OpeningHour>? = null,
    val imageUrl: String? = null,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val isActive: Boolean = true,
    val distance: Double? = null,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
) : Parcelable

@Parcelize
data class OpeningHour(
    val open: String,
    val close: String,
    val isClosed: Boolean = false
) : Parcelable

enum class StoreCategory {
    GROCERY,
    BAKERY,
    RESTAURANT,
    CAFE,
    DELI,
    OTHER
}
