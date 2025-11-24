package com.example.eco_plate.data.repository

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
    suspend fun getStoreItems(
        storeId: String,
        category: String? = null,
        available: Boolean? = null,
        page: Int? = 1,
        limit: Int? = 20
    ): Flow<Resource<List<Item>>> = flow {
        emit(Resource.Loading())
        try {
            val response = inventoryApi.getStoreItems(storeId, category, available, page, limit)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch store items"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
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
            val response = inventoryApi.createItem(item)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to create item"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun updateItem(itemId: String, item: Map<String, Any>): Flow<Resource<Item>> = flow {
        emit(Resource.Loading())
        try {
            val response = inventoryApi.updateItem(itemId, item)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update item"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
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
