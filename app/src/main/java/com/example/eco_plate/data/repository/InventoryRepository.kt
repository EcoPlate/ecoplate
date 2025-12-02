package com.example.eco_plate.data.repository

import android.util.Log
import com.example.eco_plate.data.api.InventoryApi
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryApi: InventoryApi
) {
    companion object {
        private const val TAG = "InventoryRepository"
    }
    suspend fun getStoreItems(
        storeId: String,
        category: String? = null,
        available: Boolean? = null,
        page: Int? = 1,
        limit: Int? = 20
    ): Flow<Resource<List<Item>>> = flow {
        emit(Resource.Loading())
        try {
            // Convert page/limit to skip/take
            val skip = ((page ?: 1) - 1) * (limit ?: 20)
            val take = limit ?: 20
            
            Log.d(TAG, "Fetching items for store $storeId (skip=$skip, take=$take)")
            val response = inventoryApi.getStoreItems(
                storeId = storeId, 
                category = category, 
                isAvailable = available,
                skip = skip,
                take = take
            )
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    val items = apiResponse.data.data
                    Log.d(TAG, "Fetched ${items.size} items (total: ${apiResponse.data.total})")
                    emit(Resource.Success(items))
                } else {
                    Log.e(TAG, "API returned success=false")
                    emit(Resource.Error("Failed to fetch store items"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to fetch store items: ${response.code()} - $errorBody")
                emit(Resource.Error(response.message() ?: "Failed to fetch store items"))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error fetching store items", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            Log.e(TAG, "Network error fetching store items", e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching store items", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun getItem(itemId: String): Flow<Resource<Item>> = flow {
        emit(Resource.Loading())
        try {
            val response = inventoryApi.getItem(itemId)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch item details"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun createItem(item: Map<String, Any>): Flow<Resource<Item>> = flow {
        emit(Resource.Loading())
        try {
            Log.d(TAG, "Creating item with data: $item")
            val response = inventoryApi.createItem(item)
            Log.d(TAG, "Create item response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val createdItem = response.body()!!
                Log.d(TAG, "Item created successfully: ${createdItem.name} (ID: ${createdItem.id})")
                emit(Resource.Success(createdItem))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to create item: ${response.code()} - ${response.message()} - $errorBody")
                emit(Resource.Error(errorBody ?: response.message() ?: "Failed to create item"))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error creating item", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            Log.e(TAG, "Network error creating item", e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            Log.e(TAG, "Error creating item", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun updateItem(itemId: String, item: Map<String, Any>): Flow<Resource<Item>> = flow {
        emit(Resource.Loading())
        try {
            Log.d(TAG, "Updating item $itemId with data: $item")
            val response = inventoryApi.updateItem(itemId, item)
            Log.d(TAG, "Update item response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val updatedItem = response.body()!!
                Log.d(TAG, "Item updated successfully: ${updatedItem.name}")
                emit(Resource.Success(updatedItem))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to update item: ${response.code()} - $errorBody")
                emit(Resource.Error(errorBody ?: response.message() ?: "Failed to update item"))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error updating item", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            Log.e(TAG, "Network error updating item", e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun updateItemAvailability(itemId: String, isAvailable: Boolean): Flow<Resource<Item>> = flow {
        emit(Resource.Loading())
        try {
            val availability = mapOf("isAvailable" to isAvailable)
            val response = inventoryApi.updateItemAvailability(itemId, availability)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update item availability"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun updateItemQuantity(itemId: String, quantity: Int): Flow<Resource<Item>> = flow {
        emit(Resource.Loading())
        try {
            val quantityMap = mapOf("quantity" to quantity)
            val response = inventoryApi.updateItemQuantity(itemId, quantityMap)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update item quantity"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}
