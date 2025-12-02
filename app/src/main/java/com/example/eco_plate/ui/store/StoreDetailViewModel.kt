package com.example.eco_plate.ui.store

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.data.models.Store
import com.example.eco_plate.data.repository.CartRepository
import com.example.eco_plate.data.repository.SearchRepository
import com.example.eco_plate.data.repository.StoreRepository
import com.example.eco_plate.ui.location.LocationManager
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "StoreDetailViewModel"

@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val searchRepository: SearchRepository,
    private val storeRepository: StoreRepository,
    private val locationManager: LocationManager
) : ViewModel() {
    
    val cartItemsCount = cartRepository.cartItemsCount
    
    private val _storeItems = MutableLiveData<Resource<List<Item>>>()
    val storeItems: LiveData<Resource<List<Item>>> = _storeItems
    
    private val _storeDetails = MutableLiveData<Resource<Store>>()
    val storeDetails: LiveData<Resource<Store>> = _storeDetails
    
    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery
    
    private val _filteredItems = MutableLiveData<List<Item>>()
    val filteredItems: LiveData<List<Item>> = _filteredItems
    
    private val _isSearching = MutableLiveData(false)
    val isSearching: LiveData<Boolean> = _isSearching
    
    // Track if we're syncing with backend
    private val _isSyncing = MutableLiveData(false)
    val isSyncing: LiveData<Boolean> = _isSyncing
    
    // Track pending sync to avoid showing "no results" prematurely
    private var pendingSyncQuery: String? = null
    
    private var currentStoreId: String? = null
    private var allItems: List<Item> = emptyList()
    private var searchJob: Job? = null
    
    fun loadStoreItems(storeId: String) {
        currentStoreId = storeId
        locationManager.getLastKnownLocation { location ->
            if (location == null) {
                _storeItems.value = Resource.Error("Location not available")
                return@getLastKnownLocation
            }
            
            viewModelScope.launch {
                val postalCode = locationManager.getPostalCodeFromCoordinates(location.latitude, location.longitude)
                
                searchRepository.searchItems(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = 50.0, // Large radius since we're filtering by storeId
                    storeId = storeId,
                    limit = 100,
                    postalCode = postalCode
                ).onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            val items = result.data?.data ?: emptyList()
                            allItems = items
                            _storeItems.value = Resource.Success(items)
                            _filteredItems.value = items
                            Log.d(TAG, "Loaded ${items.size} items for store $storeId")
                        }
                        is Resource.Error -> {
                            _storeItems.value = Resource.Error(result.message ?: "Error loading items")
                        }
                        is Resource.Loading -> {
                            _storeItems.value = Resource.Loading()
                        }
                    }
                }.launchIn(viewModelScope)
            }
        }
    }
    
    /**
     * Search items within the current store
     * @param forceSync If true, triggers a backend sync immediately (used when user presses Enter)
     */
    fun searchInStore(query: String, forceSync: Boolean = false) {
        _searchQuery.value = query
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _filteredItems.value = allItems
            _isSearching.value = false
            _isSyncing.value = false
            pendingSyncQuery = null
            return
        }
        
        _isSearching.value = true
        
        searchJob = viewModelScope.launch {
            // Debounce search (skip if force sync)
            if (!forceSync) {
                delay(300)
            }
            
            // First, filter from cached items
            val localResults = allItems.filter { item ->
                item.name.contains(query, ignoreCase = true) ||
                item.category?.contains(query, ignoreCase = true) == true ||
                item.brand?.contains(query, ignoreCase = true) == true ||
                item.description?.contains(query, ignoreCase = true) == true
            }
            
            // Show local results immediately if we have any
            if (localResults.isNotEmpty()) {
                _filteredItems.value = localResults
            }
            
            // Also search from API for items we might not have cached
            currentStoreId?.let { storeId ->
                locationManager.getLastKnownLocation { location ->
                    if (location != null) {
                        viewModelScope.launch {
                            val postalCode = locationManager.getPostalCodeFromCoordinates(location.latitude, location.longitude)
                            
                            // If force sync or no local results, trigger sync
                            if (forceSync || localResults.isEmpty()) {
                                Log.d(TAG, "Triggering sync for '$query' in store $storeId (forceSync=$forceSync, localResults=${localResults.size})")
                                pendingSyncQuery = query
                                _isSyncing.postValue(true)
                                triggerStoreSync(storeId, query, location.latitude, location.longitude, postalCode)
                            }
                            
                            searchRepository.searchItemsInStore(
                                storeId = storeId,
                                query = query,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                postalCode = postalCode
                            ).onEach { result ->
                                when (result) {
                                    is Resource.Success -> {
                                        val apiResults = result.data ?: emptyList()
                                        // Merge with local results, avoiding duplicates
                                        val mergedResults = (localResults + apiResults)
                                            .distinctBy { it.id }
                                            .sortedBy { it.name }
                                        
                                        // If still empty and we're syncing, don't update yet - wait for sync
                                        if (mergedResults.isEmpty() && _isSyncing.value == true && pendingSyncQuery == query) {
                                            Log.d(TAG, "No results yet but sync in progress, waiting...")
                                            // Keep showing loading via isSearching
                                        } else {
                                            _filteredItems.postValue(mergedResults)
                                            if (pendingSyncQuery != query) {
                                                _isSearching.postValue(false)
                                            }
                                        }
                                    }
                                    else -> { /* Keep local results */ }
                                }
                            }.launchIn(viewModelScope)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Trigger a sync for products matching a query within a specific store
     */
    private fun triggerStoreSync(storeId: String, query: String, latitude: Double, longitude: Double, postalCode: String?) {
        viewModelScope.launch {
            // Get store type from storeDetails
            val storeType = when {
                _storeDetails.value?.data?.name?.contains("Walmart", ignoreCase = true) == true -> "walmart"
                _storeDetails.value?.data?.name?.contains("Superstore", ignoreCase = true) == true -> "pc"
                _storeDetails.value?.data?.name?.contains("No Frills", ignoreCase = true) == true -> "pc"
                _storeDetails.value?.data?.name?.contains("Save-On", ignoreCase = true) == true -> "saveon"
                else -> null
            }
            
            searchRepository.searchAndSync(
                latitude = latitude,
                longitude = longitude,
                query = query,
                postalCode = postalCode,
                storeId = storeId,
                storeType = storeType
            ).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "Store sync completed: ${result.data?.totalProducts} products synced")
                        _isSyncing.postValue(false)
                        
                        val wasThisQuery = pendingSyncQuery == query
                        pendingSyncQuery = null
                        
                        // Re-search after sync to get updated results
                        if (wasThisQuery) {
                            searchRepository.searchItemsInStore(
                                storeId = storeId,
                                query = query,
                                latitude = latitude,
                                longitude = longitude,
                                postalCode = postalCode
                            ).onEach { searchResult ->
                                when (searchResult) {
                                    is Resource.Success -> {
                                        val items = searchResult.data ?: emptyList()
                                        // Merge with existing local items
                                        val mergedResults = (allItems.filter { item ->
                                            item.name.contains(query, ignoreCase = true) ||
                                            item.category?.contains(query, ignoreCase = true) == true
                                        } + items).distinctBy { it.id }.sortedBy { it.name }
                                        
                                        _filteredItems.postValue(mergedResults)
                                        _isSearching.postValue(false)
                                    }
                                    else -> {
                                        _isSearching.postValue(false)
                                    }
                                }
                            }.launchIn(viewModelScope)
                        }
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Store sync failed: ${result.message}")
                        _isSyncing.postValue(false)
                        _isSearching.postValue(false)
                        pendingSyncQuery = null
                    }
                    is Resource.Loading -> {
                        // Keep syncing
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    
    /**
     * Clear search and show all items
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _filteredItems.value = allItems
        _isSearching.value = false
        _isSyncing.value = false
        pendingSyncQuery = null
    }
    
    fun loadStoreDetails(storeId: String) {
        viewModelScope.launch {
            storeRepository.getStore(storeId)
                .onEach { result ->
                    _storeDetails.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun addToCart(
        storeId: String,
        storeName: String,
        productId: String,
        productName: String,
        price: Float,
        quantity: Int = 1,
        imageUrl: String? = null
    ) {
        Log.d(TAG, "Adding to cart: productId=$productId, name=$productName, qty=$quantity")
        viewModelScope.launch {
            cartRepository.addToCart(productId, quantity).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "Successfully added $productName to cart")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Error adding to cart: ${result.message}")
                    }
                    is Resource.Loading -> {
                        Log.d(TAG, "Adding to cart...")
                    }
                }
            }
        }
    }
}
