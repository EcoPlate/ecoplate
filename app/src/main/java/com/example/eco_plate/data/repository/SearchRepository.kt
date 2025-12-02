package com.example.eco_plate.data.repository

import android.util.Log
import com.example.eco_plate.data.api.SearchApi
import com.example.eco_plate.data.api.SearchSyncRequest
import com.example.eco_plate.data.api.SyncResult
import com.example.eco_plate.data.local.db.dao.ItemDao
import com.example.eco_plate.data.local.db.dao.StoreDao
import com.example.eco_plate.data.local.db.entity.CachedItem
import com.example.eco_plate.data.local.db.entity.CachedStore
import com.example.eco_plate.data.models.*
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SearchRepository"
private const val CACHE_EXPIRY_MS = 30 * 60 * 1000L // 30 minutes

@Singleton
class SearchRepository @Inject constructor(
    private val searchApi: SearchApi,
    private val itemDao: ItemDao,
    private val storeDao: StoreDao
) {
    
    /**
     * Search stores - returns cached data immediately, then fetches fresh data
     */
    suspend fun searchStores(
        latitude: Double,
        longitude: Double,
        radius: Double? = 5.0,
        category: String? = null,
        query: String? = null,
        limit: Int? = 20,
        offset: Int? = 0,
        postalCode: String? = null
    ): Flow<Resource<SearchStoresResponse>> = flow {
        // Always fetch fresh from API - no cache for location-based queries
        Log.d(TAG, "Searching stores at ($latitude, $longitude) radius=${radius}km")
        emit(Resource.Loading())
        
        // Fetch fresh data from API
        try {
            val response = searchApi.searchStores(
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                category = category,
                query = query,
                limit = limit,
                offset = offset,
                postalCode = postalCode
            )
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success) {
                    val stores = wrapper.data.data
                    Log.d(TAG, "=== STORES API RESPONSE ===")
                    Log.d(TAG, "Request: lat=$latitude, lng=$longitude, radius=${radius}km")
                    Log.d(TAG, "Got ${stores.size} stores from API")
                    stores.forEach { store ->
                        Log.d(TAG, "  - ${store.name}: (${store.latitude}, ${store.longitude}) dist=${store.distanceKm}km")
                    }
                    emit(Resource.Success(wrapper.data))
                } else {
                    Log.e(TAG, "API error: ${wrapper.message}")
                    emit(Resource.Error(wrapper.message ?: "Failed to search stores"))
                }
            } else {
                Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                emit(Resource.Error(response.message() ?: "Failed to search stores"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    /**
     * Search items - returns cached data immediately, then fetches fresh data from API
     * When user searches for something specific, always fetch from API
     */
    suspend fun searchItems(
        latitude: Double,
        longitude: Double,
        radius: Double? = 5.0,
        category: String? = null,
        query: String? = null,
        minDiscount: Int? = null,
        maxPrice: Double? = null,
        nearBestBefore: Boolean? = null,
        isClearance: Boolean? = null,
        storeType: String? = null,
        storeId: String? = null,
        limit: Int? = 20,
        offset: Int? = 0,
        postalCode: String? = null
    ): Flow<Resource<SearchItemsResponse>> = flow {
        // Always emit loading first for searches
        emit(Resource.Loading())
        
        // Check for cached items while API is loading
        val cachedItems = if (storeId != null) {
            // If searching within a store, get cached items for that store
            if (query.isNullOrBlank()) {
                itemDao.getItemsByStoreSync(storeId)
            } else {
                itemDao.searchItemsInStore(storeId, query)
            }
        } else if (!query.isNullOrBlank()) {
            // Search by query in local cache
            itemDao.searchItems(query, limit ?: 50)
        } else {
            // Get items near location
            getCachedItemsNearLocation(latitude, longitude, radius ?: 5.0)
        }
        
        // Show cached results while waiting for API (only if no specific query)
        if (cachedItems.isNotEmpty() && query.isNullOrBlank()) {
            Log.d(TAG, "Showing ${cachedItems.size} cached items while fetching from API")
            emit(Resource.Success(SearchItemsResponse(
                data = cachedItems.map { it.toItem() },
                total = cachedItems.size,
                skip = offset ?: 0,
                take = limit ?: 20
            )))
        }
        
        // ALWAYS fetch from API - especially important for new searches
        try {
            Log.d(TAG, "Fetching items from API with query: $query")
            val response = searchApi.searchItems(
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                category = category,
                query = query,
                minDiscount = minDiscount,
                maxPrice = maxPrice,
                nearBestBefore = nearBestBefore,
                isClearance = isClearance,
                storeType = storeType,
                storeId = storeId,
                limit = limit,
                offset = offset,
                postalCode = postalCode
            )
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success) {
                    // Cache the fresh data
                    val items = wrapper.data.data
                    if (items.isNotEmpty()) {
                        cacheItems(items, latitude, longitude)
                        Log.d(TAG, "Fetched and cached ${items.size} items from API")
                    } else {
                        Log.d(TAG, "API returned no items for query: $query")
                    }
                    // Always emit API results - they're the source of truth
                    emit(Resource.Success(wrapper.data))
                } else {
                    Log.w(TAG, "API search failed: ${wrapper.message}")
                    // If API fails but we have cache, show cache with a message
                    if (cachedItems.isNotEmpty()) {
                        emit(Resource.Success(SearchItemsResponse(
                            data = cachedItems.map { it.toItem() },
                            total = cachedItems.size,
                            skip = offset ?: 0,
                            take = limit ?: 20
                        )))
                    } else {
                        emit(Resource.Error(wrapper.message ?: "No items found for '$query'"))
                    }
                }
            } else {
                Log.e(TAG, "API request failed: ${response.message()}")
                if (cachedItems.isNotEmpty()) {
                    emit(Resource.Success(SearchItemsResponse(
                        data = cachedItems.map { it.toItem() },
                        total = cachedItems.size,
                        skip = offset ?: 0,
                        take = limit ?: 20
                    )))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to search items"))
                }
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error: ${e.message}")
            if (cachedItems.isNotEmpty()) {
                emit(Resource.Success(SearchItemsResponse(
                    data = cachedItems.map { it.toItem() },
                    total = cachedItems.size,
                    skip = offset ?: 0,
                    take = limit ?: 20
                )))
            } else {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "IO error: ${e.message}")
            if (cachedItems.isNotEmpty()) {
                emit(Resource.Success(SearchItemsResponse(
                    data = cachedItems.map { it.toItem() },
                    total = cachedItems.size,
                    skip = offset ?: 0,
                    take = limit ?: 20
                )))
            } else {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            if (cachedItems.isNotEmpty()) {
                emit(Resource.Success(SearchItemsResponse(
                    data = cachedItems.map { it.toItem() },
                    total = cachedItems.size,
                    skip = offset ?: 0,
                    take = limit ?: 20
                )))
            } else {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
        }
    }
    
    /**
     * Search items within a specific store (local + API)
     */
    suspend fun searchItemsInStore(
        storeId: String,
        query: String,
        latitude: Double,
        longitude: Double,
        postalCode: String? = null
    ): Flow<Resource<List<Item>>> = flow {
        // First check local cache
        val cachedItems = if (query.isBlank()) {
            itemDao.getItemsByStoreSync(storeId)
        } else {
            itemDao.searchItemsInStore(storeId, query)
        }
        
        if (cachedItems.isNotEmpty()) {
            Log.d(TAG, "Found ${cachedItems.size} cached items for store $storeId with query '$query'")
            emit(Resource.Success(cachedItems.map { it.toItem() }))
        } else {
            emit(Resource.Loading())
        }
        
        // Fetch from API
        try {
            val response = searchApi.searchItems(
                latitude = latitude,
                longitude = longitude,
                radius = 50.0, // Wide radius when searching in specific store
                query = if (query.isBlank()) null else query,
                storeId = storeId,
                limit = 100,
                postalCode = postalCode
            )
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success) {
                    val items = wrapper.data.data
                    cacheItems(items, latitude, longitude)
                    emit(Resource.Success(items))
                } else if (cachedItems.isEmpty()) {
                    emit(Resource.Error(wrapper.message ?: "No items found"))
                }
            } else if (cachedItems.isEmpty()) {
                emit(Resource.Error("Failed to search items"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "API error searching items in store: ${e.message}")
            if (cachedItems.isEmpty()) {
                emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
            }
        }
    }
    
    /**
     * Local search only - for instant search results
     */
    suspend fun searchLocalItems(query: String, limit: Int = 50): List<Item> {
        return itemDao.searchItems(query, limit).map { it.toItem() }
    }
    
    /**
     * Local search for stores only
     */
    suspend fun searchLocalStores(query: String, limit: Int = 30): List<Store> {
        return storeDao.searchStores(query, limit).map { it.toStore() }
    }
    
    /**
     * Get all cached categories
     */
    suspend fun getCachedCategories(): List<String> {
        return itemDao.getAllCategories()
    }
    
    /**
     * Get cached item count
     */
    suspend fun getCachedItemCount(): Int {
        return itemDao.getItemCount()
    }
    
    /**
     * Get cached store count
     */
    suspend fun getCachedStoreCount(): Int {
        return storeDao.getStoreCount()
    }

    suspend fun getNearbyStores(
        latitude: Double,
        longitude: Double,
        limit: Int? = 10,
        postalCode: String? = null
    ): Flow<Resource<List<Store>>> = flow {
        // First emit cached data
        val cachedStores = getCachedStoresNearLocation(latitude, longitude, 15.0)
        if (cachedStores.isNotEmpty()) {
            emit(Resource.Success(cachedStores.take(limit ?: 10).map { it.toStore() }))
        } else {
        emit(Resource.Loading())
        }
        
        try {
            val response = searchApi.getNearbyStores(latitude, longitude, limit, postalCode)
            if (response.isSuccessful && response.body() != null) {
                val stores = response.body()!!
                cacheStores(stores, latitude, longitude)
                emit(Resource.Success(stores))
            } else if (cachedStores.isEmpty()) {
                emit(Resource.Error(response.message() ?: "Failed to fetch nearby stores"))
            }
        } catch (e: HttpException) {
            if (cachedStores.isEmpty()) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
        } catch (e: IOException) {
            if (cachedStores.isEmpty()) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
            }
        } catch (e: Exception) {
            if (cachedStores.isEmpty()) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
        }
    }
    
    // Cache helpers
    
    private suspend fun cacheItems(items: List<Item>, latitude: Double, longitude: Double) {
        try {
            val cachedItems = items.map { CachedItem.fromItem(it, latitude, longitude) }
            itemDao.insertItems(cachedItems)
        } catch (e: Exception) {
            Log.e(TAG, "Error caching items: ${e.message}")
        }
    }
    
    private suspend fun cacheStores(stores: List<Store>, userLatitude: Double, userLongitude: Double) {
        try {
            val cachedStores = stores.map { CachedStore.fromStore(it, userLatitude, userLongitude) }
            storeDao.insertStores(cachedStores)
        } catch (e: Exception) {
            Log.e(TAG, "Error caching stores: ${e.message}")
        }
    }
    
    private suspend fun getCachedItemsNearLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<CachedItem> {
        // Convert radius to approximate lat/lng degrees (1 degree ≈ 111km)
        val latDelta = radiusKm / 111.0
        val lngDelta = radiusKm / (111.0 * kotlin.math.cos(Math.toRadians(latitude)))
        
        val nearbyItems = itemDao.getItemsNearLocation(
            minLat = latitude - latDelta,
            maxLat = latitude + latDelta,
            minLng = longitude - lngDelta,
            maxLng = longitude + lngDelta,
            limit = 100
        )
        
        // If no items found near location, return all recent items as fallback
        return if (nearbyItems.isEmpty()) {
            Log.d(TAG, "No items found near location, using recent items fallback")
            itemDao.getRecentItems(100)
        } else {
            nearbyItems
        }
    }
    
    private suspend fun getCachedStoresNearLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<CachedStore> {
        // Convert radius to approximate lat/lng degrees (1 degree ≈ 111km)
        val latDelta = radiusKm / 111.0
        val lngDelta = radiusKm / (111.0 * kotlin.math.cos(Math.toRadians(latitude)))
        
        val nearbyStores = storeDao.getStoresNearLocation(
            minLat = latitude - latDelta,
            maxLat = latitude + latDelta,
            minLng = longitude - lngDelta,
            maxLng = longitude + lngDelta,
            limit = 500
        )
        
        // Only return stores that are actually nearby - NO FALLBACK to all stores
        Log.d(TAG, "Found ${nearbyStores.size} cached stores within ${radiusKm}km")
        return nearbyStores
    }
    
    /**
     * Clear old cached data
     */
    suspend fun cleanupOldCache() {
        val expiryTime = System.currentTimeMillis() - CACHE_EXPIRY_MS
        itemDao.deleteOldItems(expiryTime)
        storeDao.deleteOldStores(expiryTime)
    }
    
    /**
     * Clear all cached data
     */
    suspend fun clearCache() {
        itemDao.deleteAllItems()
        storeDao.deleteAllStores()
    }
    
    /**
     * Trigger a sync for products matching a query, optionally within a specific store
     */
    suspend fun searchAndSync(
        latitude: Double,
        longitude: Double,
        query: String,
        postalCode: String? = null,
        storeId: String? = null,
        storeType: String? = null
    ): Flow<Resource<SyncResult>> = flow {
        emit(Resource.Loading())
        try {
            Log.d(TAG, "Triggering search sync for query: $query${storeId?.let { ", storeId: $it" } ?: ""}${storeType?.let { ", storeType: $it" } ?: ""}")
            val response = searchApi.searchAndSync(
                SearchSyncRequest(
                    latitude = latitude,
                    longitude = longitude,
                    query = query,
                    postalCode = postalCode,
                    storeId = storeId,
                    storeType = storeType
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!
                Log.d(TAG, "Search sync completed: ${result.totalProducts} products from ${result.stores?.size ?: 0} stores")
                emit(Resource.Success(result))
            } else {
                Log.e(TAG, "Search sync failed: ${response.message()}")
                emit(Resource.Error(response.message() ?: "Failed to sync products"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search sync error: ${e.message}")
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }
}
