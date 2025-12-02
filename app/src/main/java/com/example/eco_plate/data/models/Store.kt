package com.example.eco_plate.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import com.google.gson.annotations.SerializedName

@Parcelize
data class Store(
    val id: String,
    val ownerId: String? = null,
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val region: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val type: String? = null,  // Backend returns "type" instead of "category"
    val category: StoreCategory? = null,
    val openingHours: Map<String, OpeningHour>? = null,
    val hours: @RawValue Map<String, Any>? = null,  // Backend returns "hours" format
    val logo: String? = null,
    val banner: String? = null,
    val imageUrl: String? = null,  // Mapped from logo/banner in backend
    val rating: Double? = null,
    val reviewCount: Int? = null,
    val isActive: Boolean? = true,
    val acceptsOrders: Boolean? = true,
    val minimumOrder: Double? = null,
    val deliveryFee: Double? = null,
    val coverageRadius: Double? = null,
    val itemCount: Int? = null,
    val distance: Double? = null,
    val distanceKm: Double? = null,  // Backend nearby stores response
    val distanceMeters: Double? = null,  // Backend nearby stores response
    val distanceFormatted: String? = null,  // Backend nearby stores response
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
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
