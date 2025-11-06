package com.example.eco_plate.data.api

import com.example.eco_plate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface SearchApi {
    @GET("search/stores")
    suspend fun searchStores(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radius: Double? = null,
        @Query("category") category: String? = null,
        @Query("query") query: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<SearchStoresResponse>

    @GET("search/items")
    suspend fun searchItems(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radius: Double? = null,
        @Query("category") category: String? = null,
        @Query("query") query: String? = null,
        @Query("minDiscount") minDiscount: Int? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("isVegetarian") isVegetarian: Boolean? = null,
        @Query("isVegan") isVegan: Boolean? = null,
        @Query("isGlutenFree") isGlutenFree: Boolean? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<SearchItemsResponse>

    @GET("search/stores/nearby")
    suspend fun getNearbyStores(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("limit") limit: Int? = null
    ): Response<List<Store>>
}
