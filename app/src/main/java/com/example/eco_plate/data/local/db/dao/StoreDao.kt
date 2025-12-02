package com.example.eco_plate.data.local.db.dao

import androidx.room.*
import com.example.eco_plate.data.local.db.entity.CachedStore
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    
    @Query("SELECT * FROM cached_stores ORDER BY name ASC")
    fun getAllStores(): Flow<List<CachedStore>>
    
    @Query("SELECT * FROM cached_stores ORDER BY name ASC")
    suspend fun getAllStoresSync(): List<CachedStore>
    
    @Query("SELECT * FROM cached_stores WHERE id = :id")
    suspend fun getStoreById(id: String): CachedStore?
    
    @Query("""
        SELECT * FROM cached_stores 
        WHERE name LIKE '%' || :query || '%' 
        OR type LIKE '%' || :query || '%'
        OR city LIKE '%' || :query || '%'
        OR address LIKE '%' || :query || '%'
        ORDER BY name ASC
        LIMIT :limit
    """)
    suspend fun searchStores(query: String, limit: Int = 30): List<CachedStore>
    
    @Query("SELECT * FROM cached_stores WHERE type = :type ORDER BY name ASC")
    suspend fun getStoresByType(type: String): List<CachedStore>
    
    @Query("SELECT COUNT(*) FROM cached_stores")
    suspend fun getStoreCount(): Int
    
    @Query("SELECT DISTINCT type FROM cached_stores WHERE type IS NOT NULL ORDER BY type ASC")
    suspend fun getAllStoreTypes(): List<String>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStores(stores: List<CachedStore>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: CachedStore)
    
    @Update
    suspend fun updateStore(store: CachedStore)
    
    @Delete
    suspend fun deleteStore(store: CachedStore)
    
    @Query("DELETE FROM cached_stores WHERE cachedAt < :timestamp")
    suspend fun deleteOldStores(timestamp: Long)
    
    @Query("DELETE FROM cached_stores")
    suspend fun deleteAllStores()
    
    // Get stores near a location (approximate - within a bounding box)
    // If store doesn't have lat/lng, use the userLatitude/userLongitude (where it was cached from)
    @Query("""
        SELECT * FROM cached_stores 
        WHERE (
            (latitude IS NOT NULL AND longitude IS NOT NULL 
             AND latitude BETWEEN :minLat AND :maxLat 
             AND longitude BETWEEN :minLng AND :maxLng)
            OR
            (userLatitude IS NOT NULL AND userLongitude IS NOT NULL 
             AND userLatitude BETWEEN :minLat AND :maxLat 
             AND userLongitude BETWEEN :minLng AND :maxLng)
        )
        ORDER BY name ASC
        LIMIT :limit
    """)
    suspend fun getStoresNearLocation(
        minLat: Double, maxLat: Double,
        minLng: Double, maxLng: Double,
        limit: Int = 50
    ): List<CachedStore>
    
    // Get all stores regardless of location (fallback)
    @Query("SELECT * FROM cached_stores ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getRecentStores(limit: Int = 50): List<CachedStore>
}

