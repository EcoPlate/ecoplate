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
        @Query("skip") offset: Int? = null,
        @Query("postalCode") postalCode: String? = null
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
        @Query("storeId") storeId: String? = null,
        @Query("take") limit: Int? = null,
        @Query("skip") offset: Int? = null,
        @Query("postalCode") postalCode: String? = null
    ): Response<ApiWrapper<SearchItemsResponse>>

    @GET("search/stores/nearby")
    suspend fun getNearbyStores(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("limit") limit: Int? = null,
        @Query("postalCode") postalCode: String? = null
    ): Response<List<Store>>
    
    @retrofit2.http.POST("batch-sync/sync/search")
    suspend fun searchAndSync(
        @retrofit2.http.Body request: SearchSyncRequest
    ): Response<SyncResult>
}

data class SearchSyncRequest(
    val latitude: Double,
    val longitude: Double,
    val query: String,
    val postalCode: String? = null,
    val storeId: String? = null,
    val storeType: String? = null
)

data class SyncResult(
    val clusterId: String?,
    val postalCode: String?,
    val totalProducts: Int,
    val stores: List<SyncStoreResult>?,
    val errors: List<String>?
)

data class SyncStoreResult(
    val storeId: String,
    val storeName: String,
    val productCount: Int
)
