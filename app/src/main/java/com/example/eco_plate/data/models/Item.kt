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
    val category: ItemCategory,
    val originalPrice: Double,
    val discountedPrice: Double,
    val discountPercentage: Double,
    val quantity: Int,
    val unit: String? = null,
    val expiryDate: String? = null,
    val imageUrl: String? = null,
    val allergens: List<String>? = null,
    val nutritionInfo: NutritionInfo? = null,
    val isVegetarian: Boolean = false,
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false,
    val isAvailable: Boolean = true,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
) : Parcelable

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
    OTHER
}
