package com.example.eco_plate.data.api

import com.example.eco_plate.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface SearchApi {
    @GET("search/stores")
    suspend fun searchStores(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusKm") radius: Double? = null,
        @Query("category") category: String? = null,
        @Query("query") query: String? = null,
        @Query("take") limit: Int? = null,
        @Query("skip") offset: Int? = null
    ): Response<ApiWrapper<SearchStoresResponse>>

    @GET("search/items")
    suspend fun searchItems(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusKm") radius: Double? = null,
        @Query("category") category: String? = null,
        @Query("query") query: String? = null,
        @Query("minPrice") minDiscount: Int? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("nearBestBefore") nearBestBefore: Boolean? = null,
        @Query("isClearance") isClearance: Boolean? = null,
        @Query("storeType") storeType: String? = null,
        @Query("take") limit: Int? = null,
        @Query("skip") offset: Int? = null
    ): Response<ApiWrapper<SearchItemsResponse>>

    @GET("search/stores/nearby")
    suspend fun getNearbyStores(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("limit") limit: Int? = null
    ): Response<List<Store>>
}
