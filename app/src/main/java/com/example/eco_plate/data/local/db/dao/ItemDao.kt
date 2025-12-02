package com.example.eco_plate.data.local.db.dao

import androidx.room.*
import com.example.eco_plate.data.local.db.entity.CachedItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    
    @Query("SELECT * FROM cached_items ORDER BY cachedAt DESC")
    fun getAllItems(): Flow<List<CachedItem>>
    
    @Query("SELECT * FROM cached_items WHERE storeId = :storeId ORDER BY name ASC")
    fun getItemsByStore(storeId: String): Flow<List<CachedItem>>
    
    @Query("SELECT * FROM cached_items WHERE storeId = :storeId ORDER BY name ASC")
    suspend fun getItemsByStoreSync(storeId: String): List<CachedItem>
    
    @Query("""
        SELECT * FROM cached_items 
        WHERE storeId = :storeId 
        AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    suspend fun searchItemsInStore(storeId: String, query: String): List<CachedItem>
    
    @Query("""
        SELECT * FROM cached_items 
        WHERE name LIKE '%' || :query || '%' 
        OR category LIKE '%' || :query || '%' 
        OR brand LIKE '%' || :query || '%'
        OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
        LIMIT :limit
    """)
    suspend fun searchItems(query: String, limit: Int = 50): List<CachedItem>
    
    @Query("SELECT * FROM cached_items WHERE category = :category ORDER BY name ASC LIMIT :limit")
    suspend fun getItemsByCategory(category: String, limit: Int = 50): List<CachedItem>
    
    @Query("SELECT * FROM cached_items WHERE id = :id")
    suspend fun getItemById(id: String): CachedItem?
    
    @Query("SELECT DISTINCT category FROM cached_items WHERE category IS NOT NULL ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    @Query("SELECT COUNT(*) FROM cached_items")
    suspend fun getItemCount(): Int
    
    @Query("SELECT COUNT(*) FROM cached_items WHERE storeId = :storeId")
    suspend fun getItemCountByStore(storeId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CachedItem>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CachedItem)
    
    @Update
    suspend fun updateItem(item: CachedItem)
    
    @Delete
    suspend fun deleteItem(item: CachedItem)
    
    @Query("DELETE FROM cached_items WHERE storeId = :storeId")
    suspend fun deleteItemsByStore(storeId: String)
    
    @Query("DELETE FROM cached_items WHERE cachedAt < :timestamp")
    suspend fun deleteOldItems(timestamp: Long)
    
    @Query("DELETE FROM cached_items")
    suspend fun deleteAllItems()
    
    // Get items near a location (approximate - within a bounding box)
    // Uses the latitude/longitude where the item was cached
    @Query("""
        SELECT * FROM cached_items 
        WHERE latitude IS NOT NULL 
        AND longitude IS NOT NULL
        AND latitude BETWEEN :minLat AND :maxLat
        AND longitude BETWEEN :minLng AND :maxLng
        ORDER BY cachedAt DESC
        LIMIT :limit
    """)
    suspend fun getItemsNearLocation(
        minLat: Double, maxLat: Double,
        minLng: Double, maxLng: Double,
        limit: Int = 100
    ): List<CachedItem>
    
    // Get all items regardless of location (fallback)
    @Query("SELECT * FROM cached_items ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getRecentItems(limit: Int = 100): List<CachedItem>
}

