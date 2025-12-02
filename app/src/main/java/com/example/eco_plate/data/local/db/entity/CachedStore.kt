package com.example.eco_plate.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.eco_plate.data.models.Store

@Entity(
    tableName = "cached_stores",
    indices = [
        Index(value = ["name"]),
        Index(value = ["type"])
    ]
)
data class CachedStore(
    @PrimaryKey
    val id: String,
    val ownerId: String?,
    val name: String,
    val description: String?,
    val address: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?,
    val country: String?,
    val latitude: Double?,
    val longitude: Double?,
    val phone: String?,
    val email: String?,
    val website: String?,
    val type: String?,
    val imageUrl: String?,
    val rating: Double?,
    val isActive: Boolean?,
    val distanceKm: Double?,
    val distanceMeters: Double?,
    val distanceFormatted: String?,
    val logo: String?,
    val banner: String?,
    val region: String?,
    val postalCode: String?,
    val itemCount: Int?,
    // Metadata for cache management
    val cachedAt: Long = System.currentTimeMillis(),
    val userLatitude: Double? = null,  // Location where this store was fetched
    val userLongitude: Double? = null
) {
    fun toStore(): Store {
        return Store(
            id = id,
            ownerId = ownerId,
            name = name,
            description = description,
            address = address,
            city = city,
            state = state,
            zipCode = zipCode,
            country = country,
            latitude = latitude,
            longitude = longitude,
            phone = phone,
            email = email,
            website = website,
            type = type,
            imageUrl = imageUrl,
            rating = rating,
            isActive = isActive,
            distanceKm = distanceKm,
            distanceMeters = distanceMeters,
            distanceFormatted = distanceFormatted,
            logo = logo,
            banner = banner,
            region = region,
            postalCode = postalCode,
            itemCount = itemCount
        )
    }
    
    companion object {
        fun fromStore(store: Store, userLatitude: Double? = null, userLongitude: Double? = null): CachedStore {
            return CachedStore(
                id = store.id,
                ownerId = store.ownerId,
                name = store.name,
                description = store.description,
                address = store.address,
                city = store.city,
                state = store.state,
                zipCode = store.zipCode,
                country = store.country,
                latitude = store.latitude,
                longitude = store.longitude,
                phone = store.phone,
                email = store.email,
                website = store.website,
                type = store.type,
                imageUrl = store.imageUrl,
                rating = store.rating,
                isActive = store.isActive,
                distanceKm = store.distanceKm,
                distanceMeters = store.distanceMeters,
                distanceFormatted = store.distanceFormatted,
                logo = store.logo,
                banner = store.banner,
                region = store.region,
                postalCode = store.postalCode,
                itemCount = store.itemCount,
                userLatitude = userLatitude,
                userLongitude = userLongitude
            )
        }
    }
}

