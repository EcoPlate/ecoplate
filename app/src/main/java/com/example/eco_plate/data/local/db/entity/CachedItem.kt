package com.example.eco_plate.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.eco_plate.data.models.Item

@Entity(
    tableName = "cached_items",
    indices = [
        Index(value = ["storeId"]),
        Index(value = ["name"]),
        Index(value = ["category"])
    ]
)
data class CachedItem(
    @PrimaryKey
    val id: String,
    val storeId: String,
    val storeName: String?,
    val name: String,
    val description: String?,
    val category: String?,
    val brand: String?,
    val currentPrice: Double,
    val originalPrice: Double?,
    val discountPercent: Double?,
    val imageUrl: String?,
    val images: List<String>?,
    val unit: String?,
    val stockQuantity: Int,
    val isAvailable: Boolean,
    val isClearance: Boolean,
    val expiryDate: String?,
    val bestBefore: String?,
    val distanceMeters: Double?,
    val createdAt: String,
    val updatedAt: String,
    // Metadata for cache management
    val cachedAt: Long = System.currentTimeMillis(),
    val latitude: Double? = null,  // Location where this item was fetched
    val longitude: Double? = null
) {
    fun toItem(): Item {
        return Item(
            id = id,
            storeId = storeId,
            storeName = storeName,
            name = name,
            description = description,
            category = category,
            brand = brand,
            currentPrice = currentPrice,
            originalPrice = originalPrice,
            discountPercent = discountPercent,
            images = images,
            unit = unit,
            stockQuantity = stockQuantity,
            isAvailable = isAvailable,
            isClearance = isClearance,
            expiryDate = expiryDate,
            bestBefore = bestBefore,
            distanceMeters = distanceMeters,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromItem(item: Item, latitude: Double? = null, longitude: Double? = null): CachedItem {
            return CachedItem(
                id = item.id,
                storeId = item.storeId,
                storeName = item.storeName,
                name = item.name,
                description = item.description,
                category = item.category,
                brand = item.brand,
                currentPrice = item.currentPrice,
                originalPrice = item.originalPrice,
                discountPercent = item.discountPercent,
                imageUrl = item.imageUrl,
                images = item.images,
                unit = item.unit,
                stockQuantity = item.stockQuantity,
                isAvailable = item.isAvailable,
                isClearance = item.isClearance,
                expiryDate = item.expiryDate,
                bestBefore = item.bestBefore,
                distanceMeters = item.distanceMeters,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt,
                latitude = latitude,
                longitude = longitude
            )
        }
    }
}
