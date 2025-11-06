package com.example.eco_plate.data.models

import com.google.gson.annotations.SerializedName

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

data class SearchStoresResponse(
    val stores: List<Store>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class SearchItemsResponse(
    val items: List<Item>,
    val total: Int,
    val page: Int,
    val limit: Int
)
