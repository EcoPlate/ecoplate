package com.example.eco_plate.ui.newItem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.local.TokenManager
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.data.repository.InventoryRepository
import com.example.eco_plate.data.repository.StoreRepository
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewItemViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val storeRepository: StoreRepository,
    private val tokenManager: TokenManager
): ViewModel() {
    
    private val _createItemState = MutableLiveData<Resource<Item>>()
    val createItemState: LiveData<Resource<Item>> = _createItemState
    
    private val _storeId = MutableLiveData<String?>()
    val storeId: LiveData<String?> = _storeId
    
    init {
        loadStoreId()
    }
    
    private fun loadStoreId() {
        viewModelScope.launch {
            // First check if we have a saved store ID
            val savedStoreId = tokenManager.storeId.first()
            if (savedStoreId != null) {
                _storeId.value = savedStoreId
            } else {
                // If not, load the user's stores
                storeRepository.getMyStores()
                    .onEach { result ->
                        if (result is Resource.Success) {
                            val stores = result.data
                            if (!stores.isNullOrEmpty()) {
                                // Use the first store
                                val firstStore = stores.first()
                                _storeId.value = firstStore.id
                                tokenManager.saveStoreId(firstStore.id)
                            }
                        }
                    }
                    .launchIn(viewModelScope)
            }
        }
    }
    
    fun createItem(
        name: String,
        description: String?,
        category: String,
        originalPrice: Double,
        currentPrice: Double,
        stockQuantity: Int,
        unit: String = "unit",
        barcode: String? = null,
        sku: String? = null,
        imageUrls: List<String>? = null,
        expiryDate: String? = null,
        bestBefore: String? = null,
        isClearance: Boolean = false,
        tags: List<String>? = null,
        allergens: List<String>? = null,
        calories: Int? = null,
        protein: Double? = null,
        carbs: Double? = null,
        fat: Double? = null,
        fiber: Double? = null,
        sugar: Double? = null,
        sodium: Double? = null
    ) {
        viewModelScope.launch {
            // Get or fetch store ID
            var currentStoreId = _storeId.value
            
            if (currentStoreId == null) {
                // Try to fetch the store for this user
                _createItemState.value = Resource.Loading()
                
                try {
                    // Get first emission from the flow
                    val result = storeRepository.getMyStores().first()
                    
                    when (result) {
                        is Resource.Success -> {
                            val stores = result.data
                            if (!stores.isNullOrEmpty()) {
                                currentStoreId = stores.first().id
                                _storeId.value = currentStoreId
                                tokenManager.saveStoreId(currentStoreId)
                            } else {
                                // No stores found - this user needs to create a store first
                                _createItemState.value = Resource.Error("No store found. Store owners need to create a store first through the website or contact support.")
                                return@launch
                            }
                        }
                        is Resource.Error -> {
                            _createItemState.value = Resource.Error("Failed to fetch store: ${result.message}")
                            return@launch
                        }
                        is Resource.Loading -> {
                            // Should not happen with first()
                            _createItemState.value = Resource.Error("Unexpected loading state")
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    _createItemState.value = Resource.Error("Error fetching store: ${e.message}")
                    return@launch
                }
            }
            
            // If we still don't have a store ID, return
            if (currentStoreId == null) {
                _createItemState.value = Resource.Error("Unable to determine store. Please try logging out and back in.")
                return@launch
            }
            val itemData = mutableMapOf<String, Any>(
                "storeId" to currentStoreId,
                "name" to name,
                "category" to category.toUpperCase(),  // Backend expects uppercase categories
                "originalPrice" to originalPrice,
                "currentPrice" to currentPrice,
                "stockQuantity" to stockQuantity,
                "unit" to unit,
                "isAvailable" to true
            )
            
            description?.let { itemData["description"] = it }
            barcode?.let { itemData["barcode"] = it }
            sku?.let { itemData["sku"] = it }
            imageUrls?.let { itemData["images"] = it }
            expiryDate?.let { itemData["expiryDate"] = it }
            bestBefore?.let { itemData["bestBefore"] = it }
            if (isClearance) { itemData["isClearance"] = true }
            tags?.let { itemData["tags"] = it }
            allergens?.let { itemData["allergens"] = it }
            
            // Add nutrition info if provided
            if (calories != null || protein != null || carbs != null || fat != null) {
                val nutritionInfo = mutableMapOf<String, Any?>()
                calories?.let { nutritionInfo["calories"] = it }
                protein?.let { nutritionInfo["protein"] = it }
                carbs?.let { nutritionInfo["carbs"] = it }
                fat?.let { nutritionInfo["fat"] = it }
                fiber?.let { nutritionInfo["fiber"] = it }
                sugar?.let { nutritionInfo["sugar"] = it }
                sodium?.let { nutritionInfo["sodium"] = it }
                itemData["nutritionInfo"] = nutritionInfo
            }
            
            // Create the item with the store ID
            inventoryRepository.createItem(itemData)
                .onEach { result ->
                    _createItemState.value = result
                }
                .launchIn(viewModelScope)
        }
    }
}