package com.example.eco_plate.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.data.models.SearchItemsResponse
import com.example.eco_plate.data.models.SearchStoresResponse
import com.example.eco_plate.data.models.Store
import com.example.eco_plate.data.repository.CartItem
import com.example.eco_plate.data.repository.CartRepository
import com.example.eco_plate.data.repository.SearchRepository
import com.example.eco_plate.ui.location.LocationManager
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SearchViewModel"

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val locationManager: LocationManager,
    private val cartRepository: CartRepository
) : ViewModel() {
    
    val cartItemsCount = cartRepository.cartItemsCount
    
    private val _itemSearchResults = MutableLiveData<Resource<SearchItemsResponse>>()
    val itemSearchResults: LiveData<Resource<SearchItemsResponse>> = _itemSearchResults
    
    private val _storeSearchResults = MutableLiveData<Resource<SearchStoresResponse>>()
    val storeSearchResults: LiveData<Resource<SearchStoresResponse>> = _storeSearchResults
    
    private val _nearbyStores = MutableLiveData<Resource<List<Store>>>()
    val nearbyStores: LiveData<Resource<List<Store>>> = _nearbyStores
    
    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery
    
    // Local instant search results
    private val _localItemResults = MutableLiveData<List<Item>>()
    val localItemResults: LiveData<List<Item>> = _localItemResults
    
    private val _localStoreResults = MutableLiveData<List<Store>>()
    val localStoreResults: LiveData<List<Store>> = _localStoreResults
    
    private val _isSearching = MutableLiveData(false)
    val isSearching: LiveData<Boolean> = _isSearching
    
    // Track if we're syncing with backend
    private val _isSyncing = MutableLiveData(false)
    val isSyncing: LiveData<Boolean> = _isSyncing
    
    // Track pending sync queries to avoid showing "no results" prematurely
    private var pendingSyncQuery: String? = null
    
    // Cache stats
    private val _cachedItemCount = MutableLiveData(0)
    val cachedItemCount: LiveData<Int> = _cachedItemCount
    
    private val _cachedStoreCount = MutableLiveData(0)
    val cachedStoreCount: LiveData<Int> = _cachedStoreCount
    
    private var searchJob: Job? = null
    
    init {
        // Load cache stats on init
        viewModelScope.launch {
            _cachedItemCount.value = searchRepository.getCachedItemCount()
            _cachedStoreCount.value = searchRepository.getCachedStoreCount()
        }
    }
    
    /**
     * Instant local search - searches cached data immediately
     */
    fun instantSearch(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            _localItemResults.value = emptyList()
            _localStoreResults.value = emptyList()
            return
        }
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            // Debounce
            delay(150)
            
            _isSearching.value = true
            
            // Search local cache first
            val localItems = searchRepository.searchLocalItems(query, 20)
            val localStores = searchRepository.searchLocalStores(query, 10)
            
            _localItemResults.value = localItems
            _localStoreResults.value = localStores
            
            Log.d(TAG, "Local search for '$query': ${localItems.size} items, ${localStores.size} stores")
            
            _isSearching.value = false
        }
    }
    
    fun searchItems(
        query: String? = null,
        category: String? = null,
        radius: Double? = 5.0,
        minDiscount: Int? = null,
        maxPrice: Double? = null,
        nearBestBefore: Boolean? = null,
        isClearance: Boolean? = null,
        storeType: String? = null,
        storeId: String? = null,
        limit: Int? = 20,
        offset: Int? = 0,
        forceSync: Boolean = false
    ) {
        _searchQuery.value = query ?: ""
        
        locationManager.getLastKnownLocation { location ->
            if (location == null) {
                _itemSearchResults.value = Resource.Error("Location not available. Please enable location services.")
                return@getLastKnownLocation
            }
            
            viewModelScope.launch {
                // Get postal code from Android's built-in Geocoder
                val postalCode = locationManager.getPostalCodeFromCoordinates(location.latitude, location.longitude)
                
                // If forceSync is true and we have a query, trigger sync first
                if (forceSync && !query.isNullOrBlank()) {
                    Log.d(TAG, "Force sync requested for query: $query")
                    triggerSearchSync(query, location.latitude, location.longitude, postalCode, storeId, storeType)
                }
                
                searchRepository.searchItems(
                    latitude = location.latitude,
                    longitude = location.longitude,
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
                ).onEach { result ->
                    // Update cache count after API fetch
                    if (result is Resource.Success) {
                        _cachedItemCount.value = searchRepository.getCachedItemCount()
                        
                        val isEmpty = result.data?.data?.isEmpty() == true
                        
                        if (!isEmpty) {
                            // Has results - show immediately and stop syncing indicator
                            Log.d(TAG, "Got ${result.data?.data?.size} results for '$query', displaying...")
                            pendingSyncQuery = null
                            _isSyncing.postValue(false)
                            _itemSearchResults.postValue(result)
                        } else if (!query.isNullOrBlank()) {
                            // No results and there's a query - trigger a sync if not already syncing
                            if (_isSyncing.value != true) {
                                Log.d(TAG, "No results found for '$query', triggering sync...")
                                pendingSyncQuery = query
                                _isSyncing.postValue(true)
                                _itemSearchResults.postValue(Resource.Loading()) // Keep showing loading
                                triggerSearchSync(query, location.latitude, location.longitude, postalCode, storeId, storeType)
                            }
                            // If we're already syncing for this query, keep showing loading
                            else if (pendingSyncQuery == query) {
                                _itemSearchResults.postValue(Resource.Loading())
                            }
                            // Sync completed for this query but still empty - show the result
                            else {
                                _isSyncing.postValue(false)
                                _itemSearchResults.postValue(result)
                            }
                        } else {
                            // No query - show empty state
                            _itemSearchResults.postValue(result)
                        }
                    } else {
                        _itemSearchResults.postValue(result)
                    }
                }.launchIn(viewModelScope)
            }
        }
    }
    
    /**
     * Trigger a sync to search for products that might not be in the database
     */
    private fun triggerSearchSync(
        query: String,
        latitude: Double,
        longitude: Double,
        postalCode: String?,
        storeId: String? = null,
        storeType: String? = null
    ) {
        viewModelScope.launch {
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
                        Log.d(TAG, "Search sync completed: ${result.data?.totalProducts} products synced")
                        _isSyncing.postValue(false)
                        
                        // Clear pending query and re-search to get updated results
                        val wasThisQuery = pendingSyncQuery == query
                        pendingSyncQuery = null
                        
                        // Always re-search after sync completes to show final results
                        if (wasThisQuery) {
                            // Re-search without triggering another sync
                            searchRepository.searchItems(
                                latitude = latitude,
                                longitude = longitude,
                                query = query,
                                storeId = storeId,
                                storeType = storeType,
                                postalCode = postalCode
                            ).onEach { searchResult ->
                                // This time, show results even if empty (sync already done)
                                _itemSearchResults.postValue(searchResult)
                            }.launchIn(viewModelScope)
                        }
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Search sync failed: ${result.message}")
                        _isSyncing.postValue(false)
                        pendingSyncQuery = null
                        // Show error or empty state
                        _itemSearchResults.postValue(Resource.Error(result.message ?: "Sync failed"))
                    }
                    is Resource.Loading -> {
                        // Keep showing loading
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    
    /**
     * Search items within a specific store
     */
    fun searchItemsInStore(
        storeId: String,
        storeName: String? = null,
        query: String? = null,
        limit: Int? = 50
    ) {
        _searchQuery.value = query ?: ""
        
        locationManager.getLastKnownLocation { location ->
            if (location == null) {
                _itemSearchResults.value = Resource.Error("Location not available. Please enable location services.")
                return@getLastKnownLocation
            }
            
            viewModelScope.launch {
                val postalCode = locationManager.getPostalCodeFromCoordinates(location.latitude, location.longitude)
                
                // Determine store type from store name if available
                val storeType = when {
                    storeName?.contains("Walmart", ignoreCase = true) == true -> "walmart"
                    storeName?.contains("Superstore", ignoreCase = true) == true -> "pc"
                    storeName?.contains("No Frills", ignoreCase = true) == true -> "pc"
                    storeName?.contains("Save-On", ignoreCase = true) == true -> "saveon"
                    else -> null
                }
                
                searchRepository.searchItems(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = 50.0, // Large radius when filtering by store
                    query = query,
                    storeId = storeId,
                    storeType = storeType,
                    limit = limit,
                    postalCode = postalCode
                ).onEach { result ->
                    _itemSearchResults.value = result
                    
                    if (result is Resource.Success) {
                        _cachedItemCount.value = searchRepository.getCachedItemCount()
                        
                        // If no results, trigger a sync for this store
                        if (result.data?.data?.isEmpty() == true) {
                            Log.d(TAG, "No items found in store $storeId, triggering sync...")
                            triggerSearchSync(
                                query = query ?: "grocery",
                                latitude = location.latitude,
                                longitude = location.longitude,
                                postalCode = postalCode,
                                storeId = storeId,
                                storeType = storeType
                            )
                        }
                    }
                }.launchIn(viewModelScope)
            }
        }
    }
    
    fun searchStores(
        query: String? = null,
        category: String? = null,
        radius: Double? = 5.0,
        limit: Int? = 20,
        offset: Int? = 0
    ) {
        _searchQuery.value = query ?: ""
        
        locationManager.getLastKnownLocation { location ->
            if (location == null) {
                _storeSearchResults.value = Resource.Error("Location not available. Please enable location services.")
                return@getLastKnownLocation
            }
            
            viewModelScope.launch {
                // Get postal code from Android's built-in Geocoder
                val postalCode = locationManager.getPostalCodeFromCoordinates(location.latitude, location.longitude)
                
                searchRepository.searchStores(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = radius,
                    category = category,
                    query = query,
                    limit = limit,
                    offset = offset,
                    postalCode = postalCode
                ).onEach { result ->
                    _storeSearchResults.value = result
                    
                    // Update cache count after API fetch
                    if (result is Resource.Success) {
                        _cachedStoreCount.value = searchRepository.getCachedStoreCount()
                    }
                }.launchIn(viewModelScope)
            }
        }
    }
    
    fun getNearbyStores(limit: Int? = 10) {
        locationManager.getLastKnownLocation { location ->
            if (location == null) {
                _nearbyStores.value = Resource.Error("Location not available. Please enable location services.")
                return@getLastKnownLocation
            }
            
            viewModelScope.launch {
                // Get postal code from Android's built-in Geocoder
                val postalCode = locationManager.getPostalCodeFromCoordinates(location.latitude, location.longitude)
                
                // Use searchStores which has proper API wrapper format
                searchRepository.searchStores(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = 15.0,
                    limit = limit ?: 20,
                    postalCode = postalCode
                ).onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            _nearbyStores.value = Resource.Success(result.data?.data ?: emptyList())
                            _cachedStoreCount.value = searchRepository.getCachedStoreCount()
                        }
                        is Resource.Error -> {
                            _nearbyStores.value = Resource.Error(result.message ?: "Error loading stores")
                        }
                        is Resource.Loading -> {
                            _nearbyStores.value = Resource.Loading()
                        }
                    }
                }.launchIn(viewModelScope)
            }
        }
    }
    
    fun clearSearchResults() {
        _searchQuery.value = ""
        _localItemResults.value = emptyList()
        _localStoreResults.value = emptyList()
        _itemSearchResults.value = Resource.Success(SearchItemsResponse(
            data = emptyList(),
            total = 0,
            skip = 0,
            take = 20
        ))
        _storeSearchResults.value = Resource.Success(SearchStoresResponse(
            data = emptyList(),
            total = 0,
            skip = 0,
            take = 20
        ))
    }
    
    /**
     * Clear all cached data
     */
    fun clearCache() {
        viewModelScope.launch {
            searchRepository.clearCache()
            _cachedItemCount.value = 0
            _cachedStoreCount.value = 0
        }
    }
    
    /**
     * Clean up old cached data
     */
    fun cleanupCache() {
        viewModelScope.launch {
            searchRepository.cleanupOldCache()
            _cachedItemCount.value = searchRepository.getCachedItemCount()
            _cachedStoreCount.value = searchRepository.getCachedStoreCount()
        }
    }
    
    /**
     * Add item to cart
     */
    fun addToCart(
        id: String,
        name: String,
        storeName: String,
        price: Float,
        originalPrice: Float? = null,
        quantity: Int = 1,
        imageUrl: String? = null,
        isEcoFriendly: Boolean = false
    ) {
        Log.d(TAG, "Adding to cart: itemId=$id, name=$name, qty=$quantity")
        viewModelScope.launch {
            cartRepository.addToCart(id, quantity).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "Successfully added $name to cart")
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
