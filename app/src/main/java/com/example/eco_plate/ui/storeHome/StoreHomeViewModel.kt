package com.example.eco_plate.ui.storeHome

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.local.TokenManager
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.data.models.Store
import com.example.eco_plate.data.repository.CartItem
import com.example.eco_plate.data.repository.CartRepository
import com.example.eco_plate.data.repository.InventoryRepository
import com.example.eco_plate.data.repository.StoreRepository
import com.example.eco_plate.ui.search.SearchProduct
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreHomeViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val storeRepository: StoreRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "StoreHomeViewModel"
    }
    
    private val _storeItems = MutableLiveData<Resource<List<Item>>>()
    val storeItems: LiveData<Resource<List<Item>>> = _storeItems
    
    private val _currentStore = MutableLiveData<Resource<Store>>()
    val currentStore: LiveData<Resource<Store>> = _currentStore
    
    private val _storeId = MutableLiveData<String?>()
    val storeId: LiveData<String?> = _storeId
    
    // Track if we need to refresh on next load
    private var needsRefresh = false
    
    // Track if we've already loaded data
    private var hasLoadedData = false
    
    init {
        loadStoreId()
    }
    
    private fun loadStoreId() {
        // Don't reload if we already have data
        if (hasLoadedData && _storeItems.value is Resource.Success) {
            Log.d(TAG, "Data already loaded, skipping reload")
            return
        }
        
        viewModelScope.launch {
            // First check if we have a saved store ID
            val savedStoreId = tokenManager.storeId.first()
            if (savedStoreId != null) {
                _storeId.value = savedStoreId
                
                // Try to use cached store info first
                val cachedStore = tokenManager.getCachedStoreInfo()
                if (cachedStore != null && cachedStore.id == savedStoreId) {
                    Log.d(TAG, "Using cached store info: ${cachedStore.name}")
                    // Create a Store object from cached info
                    val store = Store(
                        id = cachedStore.id,
                        name = cachedStore.name,
                        address = cachedStore.address,
                        phone = cachedStore.phone,
                        description = cachedStore.description,
                        imageUrl = cachedStore.imageUrl,
                        rating = cachedStore.rating
                    )
                    _currentStore.value = Resource.Success(store)
                    // Load items (always fetch fresh items)
                    loadStoreItems(savedStoreId)
                } else {
                    // No cached info, fetch from API
                    loadStoreData(savedStoreId, cacheResult = true)
                    loadStoreItems(savedStoreId)
                }
                hasLoadedData = true
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
                                // Save store info to cache
                                tokenManager.saveStoreInfo(
                                    storeId = firstStore.id,
                                    name = firstStore.name,
                                    address = firstStore.address,
                                    phone = firstStore.phone,
                                    description = firstStore.description,
                                    imageUrl = firstStore.imageUrl,
                                    rating = firstStore.rating
                                )
                                _currentStore.value = Resource.Success(firstStore)
                                loadStoreItems(firstStore.id)
                                hasLoadedData = true
                            }
                        }
                    }
                    .launchIn(viewModelScope)
            }
        }
    }
    
    fun loadStoreData(storeId: String, cacheResult: Boolean = false) {
        viewModelScope.launch {
            storeRepository.getStore(storeId)
                .onEach { result ->
                    _currentStore.value = result
                    // Cache the store info on successful fetch
                    if (cacheResult && result is Resource.Success) {
                        result.data?.let { store ->
                            Log.d(TAG, "Caching store info: ${store.name}")
                            tokenManager.saveStoreInfo(
                                storeId = store.id,
                                name = store.name,
                                address = store.address,
                                phone = store.phone,
                                description = store.description,
                                imageUrl = store.imageUrl,
                                rating = store.rating
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun loadStoreItems(
        storeId: String,
        category: String? = null,
        available: Boolean? = null
    ) {
        Log.d(TAG, "Loading store items for store: $storeId")
        viewModelScope.launch {
            inventoryRepository.getStoreItems(
                storeId = storeId,
                category = category,
                available = available,
                limit = 100
            ).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "Loaded ${result.data?.size ?: 0} items for store $storeId")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Error loading store items: ${result.message}")
                    }
                    is Resource.Loading -> {
                        Log.d(TAG, "Loading store items...")
                    }
                }
                _storeItems.value = result
            }.launchIn(viewModelScope)
        }
    }
    
    /**
     * Mark that a refresh is needed (e.g., after creating a new item)
     */
    fun markNeedsRefresh() {
        needsRefresh = true
    }
    
    /**
     * Check if refresh is needed and perform it
     */
    fun checkAndRefresh() {
        if (needsRefresh) {
            needsRefresh = false
            refreshStore()
        }
    }
    
    fun refreshStore() {
        Log.d(TAG, "Refreshing store data and items")
        _storeId.value?.let { id ->
            loadStoreData(id, cacheResult = true)
            loadStoreItems(id)
        }
    }
    
    fun updateItemAvailability(itemId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            inventoryRepository.updateItemAvailability(itemId, isAvailable)
                .onEach { result ->
                    if (result is Resource.Success) {
                        // Refresh the items list
                        _storeId.value?.let { loadStoreItems(it) }
                    }
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun updateItemQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch {
            inventoryRepository.updateItemQuantity(itemId, quantity)
                .onEach { result ->
                    if (result is Resource.Success) {
                        // Refresh the items list
                        _storeId.value?.let { loadStoreItems(it) }
                    }
                }
                .launchIn(viewModelScope)
        }
    }
    
    private val _updateItemState = MutableLiveData<Resource<Item>>()
    val updateItemState: LiveData<Resource<Item>> = _updateItemState
    
    /**
     * Update an item with new values (quantity, expiry date, price, etc.)
     * Note: The backend calculates discount from currentPrice and originalPrice
     */
    fun updateItem(
        itemId: String,
        stockQuantity: Int? = null,
        expiryDate: String? = null,
        bestBefore: String? = null,
        currentPrice: Double? = null,
        originalPrice: Double? = null,
        isClearance: Boolean? = null
    ) {
        viewModelScope.launch {
            Log.d(TAG, "Updating item $itemId: quantity=$stockQuantity, expiry=$expiryDate, currentPrice=$currentPrice")
            
            val updateData = mutableMapOf<String, Any>()
            stockQuantity?.let { updateData["stockQuantity"] = it }
            expiryDate?.let { updateData["expiryDate"] = it }
            bestBefore?.let { updateData["bestBefore"] = it }
            currentPrice?.let { updateData["currentPrice"] = it }
            originalPrice?.let { updateData["originalPrice"] = it }
            isClearance?.let { updateData["isClearance"] = it }
            
            if (updateData.isEmpty()) {
                Log.w(TAG, "No update data provided")
                return@launch
            }
            
            inventoryRepository.updateItem(itemId, updateData)
                .onEach { result ->
                    _updateItemState.value = result
                    when (result) {
                        is Resource.Success -> {
                            Log.d(TAG, "Item updated successfully: ${result.data?.name}")
                            // Refresh the items list
                            _storeId.value?.let { loadStoreItems(it) }
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Failed to update item: ${result.message}")
                        }
                        else -> {}
                    }
                }
                .launchIn(viewModelScope)
        }
    }
}