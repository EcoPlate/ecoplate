package com.example.eco_plate.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class Item(
    val id: String,
    val storeId: String,
    val store: Store? = null,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val subcategory: String? = null,
    val originalPrice: Double? = null,
    val currentPrice: Double,
    val discountPercent: Double? = null,
    val sku: String? = null,
    val barcode: String? = null,
    val stockQuantity: Int = 0,
    val unit: String? = null,
    val bestBefore: String? = null,
    val expiryDate: String? = null,
    val isClearance: Boolean = false,
    val images: List<String>? = null,
    val nutritionInfo: NutritionInfo? = null,
    val allergens: List<String>? = null,
    val tags: List<String>? = null,
    val isAvailable: Boolean = true,
    val availableFrom: String? = null,
    val availableUntil: String? = null,
    val createdAt: String,
    val updatedAt: String,
    // Store details included in response
    val storeName: String? = null,
    val storeType: String? = null,
    val storeAddress: String? = null,
    val storeLatitude: Double? = null,
    val storeLongitude: Double? = null,
    val distanceMeters: Double? = null,
    //NEW ADDITIONS
    val brand: String? = null,
    val upc: String? = null,
    val plu: Int? = null,
    val soldByWeight: Boolean = false
) : Parcelable {
    // Compatibility properties for old field names
    val discountedPrice: Double get() = currentPrice
    val discountPercentage: Double get() = discountPercent ?: 0.0
    val quantity: Int get() = stockQuantity
    val imageUrl: String? get() = images?.firstOrNull()
    
    // For dietary restrictions - check tags list
    val isVegetarian: Boolean get() = tags?.contains("vegetarian") == true
    val isVegan: Boolean get() = tags?.contains("vegan") == true
    val isGlutenFree: Boolean get() = tags?.contains("gluten-free") == true
}

@Parcelize
data class NutritionInfo(
    val calories: Int? = null,
    val protein: Double? = null,
    val carbohydrates: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodium: Double? = null
) : Parcelable

enum class ItemCategory {
    FRUITS,
    VEGETABLES,
    DAIRY,
    MEAT,
    SEAFOOD,
    BAKERY,
    DELI,
    FROZEN,
    PANTRY,
    BEVERAGES,
    SNACKS,
    GROCERY,
    OTHER
}
