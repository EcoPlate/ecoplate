package com.example.eco_plate.data.models

import com.google.gson.annotations.SerializedName

// Wrapper for backend API responses
data class ApiWrapper<T>(
    val success: Boolean,
    val data: T,
    val timestamp: String? = null,
    val path: String? = null,
    val message: String? = null
)

data class SearchStoresRequest(
    val latitude: Double,
    val longitude: Double,
    val radius: Double? = 5.0, // km
    val category: String? = null,
    val query: String? = null,
    val limit: Int? = 20,
    val offset: Int? = 0
)

data class SearchItemsRequest(
    val latitude: Double,
    val longitude: Double,
    val radius: Double? = 5.0, // km
    val category: String? = null,
    val query: String? = null,
    val minDiscount: Int? = null,
    val maxPrice: Double? = null,
    val isVegetarian: Boolean? = null,
    val isVegan: Boolean? = null,
    val isGlutenFree: Boolean? = null,
    val limit: Int? = 20,
    val offset: Int? = 0
)

// Search stores response structure
data class SearchStoresResponse(
    val data: List<Store>,
    val total: Int,
    val skip: Int,
    val take: Int
)

// Search items response structure  
data class SearchItemsResponse(
    val data: List<Item>,
    val total: Int,
    val skip: Int,
    val take: Int
)
