package com.example.eco_plate.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.Store
import com.example.eco_plate.data.models.Item
import android.util.Log
import com.example.eco_plate.data.repository.AddressRepository
import com.example.eco_plate.data.repository.CartRepository
import com.example.eco_plate.data.repository.SearchRepository
import com.example.eco_plate.data.repository.StoreRepository
import com.example.eco_plate.ui.location.LocationManager
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val storeRepository: StoreRepository,
    private val cartRepository: CartRepository,
    private val locationManager: LocationManager,
    private val addressRepository: AddressRepository
) : ViewModel() {

    private val _nearbyStores = MutableLiveData<Resource<List<Store>>>()
    val nearbyStores: LiveData<Resource<List<Store>>> = _nearbyStores

    private val _featuredItems = MutableLiveData<Resource<List<Item>>>()
    val featuredItems: LiveData<Resource<List<Item>>> = _featuredItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadNearbyStores(latitude: Double, longitude: Double) {
        Log.d(TAG, "loadNearbyStores called: $latitude, $longitude")
        
        // Clear previous stores immediately
        _nearbyStores.value = Resource.Loading()
        
        viewModelScope.launch {
            val postalCode = locationManager.getPostalCodeFromCoordinates(latitude, longitude)
            
            searchRepository.searchStores(
                latitude = latitude,
                longitude = longitude,
                radius = 10.0,  // 10km radius for home page
                limit = 15,     // Get extra to filter
                postalCode = postalCode
            ).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        // Filter out stores at exact same location as delivery address (bad data)
                        val filteredStores = (result.data?.data ?: emptyList()).filter { store ->
                            val storeLat = store.latitude ?: return@filter false
                            val storeLng = store.longitude ?: return@filter false
                            val latDiff = kotlin.math.abs(storeLat - latitude)
                            val lngDiff = kotlin.math.abs(storeLng - longitude)
                            val isAtDeliveryAddress = latDiff < 0.0001 && lngDiff < 0.0001
                            if (isAtDeliveryAddress) {
                                Log.w(TAG, "Filtering out store '${store.name}' - at delivery address")
                            }
                            !isAtDeliveryAddress
                        }.take(10)
                        
                        Log.d(TAG, "Showing ${filteredStores.size} stores for home page")
                        _nearbyStores.value = Resource.Success(filteredStores)
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Error loading stores: ${result.message}")
                        _nearbyStores.value = Resource.Error(result.message ?: "Error loading stores")
                    }
                    is Resource.Loading -> {
                        // Already set to loading above
                    }
                }
                _isLoading.value = result is Resource.Loading
            }.launchIn(viewModelScope)
        }
    }

    fun searchItems(
        latitude: Double,
        longitude: Double,
        category: String? = null,
        query: String? = null,
        minDiscount: Int? = null,  // Make discount optional
        limit: Int = 50  // Get more products to show on home
    ) {
        viewModelScope.launch {
            // Get postal code from Android's built-in Geocoder
            val postalCode = locationManager.getPostalCodeFromCoordinates(latitude, longitude)
            
            searchRepository.searchItems(
                latitude = latitude,
                longitude = longitude,
                category = category,
                query = query,
                minDiscount = minDiscount,  // Don't force discount filter
                limit = limit,  // Pass limit to get more products
                postalCode = postalCode
            ).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _featuredItems.value = Resource.Success(result.data?.data)
                    }
                    is Resource.Error -> {
                        _featuredItems.value = Resource.Error(result.message ?: "Error loading items")
                    }
                    is Resource.Loading -> {
                        _featuredItems.value = Resource.Loading()
                    }
                }
                _isLoading.value = result is Resource.Loading
            }.launchIn(viewModelScope)
        }
    }

    fun getAllStores(page: Int = 1, category: String? = null) {
        viewModelScope.launch {
            storeRepository.getStores(page, 20, category)
                .onEach { result ->
                    _nearbyStores.value = result
                    _isLoading.value = result is Resource.Loading
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
        imageUrl: String? = null,
        isEcoFriendly: Boolean = false
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

    // Delivery address - null means user needs to add one
    private val _deliveryAddress = MutableStateFlow<String?>(null)
    val deliveryAddress = _deliveryAddress.asStateFlow()
    
    // Delivery address label (Home, Work, etc.)
    private val _deliveryLabel = MutableStateFlow<String?>(null)
    val deliveryLabel = _deliveryLabel.asStateFlow()
    
    // Whether we've loaded the initial address
    private val _hasLoadedAddress = MutableStateFlow(false)
    val hasLoadedAddress = _hasLoadedAddress.asStateFlow()
    
    // Current delivery location coordinates
    private var currentDeliveryLat: Double? = null
    private var currentDeliveryLng: Double? = null

    init {
        // Load user's default address on init
        loadDefaultAddress()
    }
    
    /**
     * Load user's default/saved address from backend
     */
    fun loadDefaultAddress() {
        Log.d(TAG, "loadDefaultAddress() called")
        viewModelScope.launch {
            try {
                // Get the first non-loading result from the default address endpoint
                val result = addressRepository.getDefaultAddress().first { it !is Resource.Loading }
                
                when (result) {
                    is Resource.Success -> {
                        val defaultAddress = result.data
                        if (defaultAddress != null) {
                            Log.d(TAG, "Got default address from API: ${defaultAddress.label} - ${defaultAddress.shortAddress}")
                            setAddressFromUserAddress(defaultAddress)
                        } else {
                            Log.d(TAG, "No default address from API, trying all addresses")
                            loadFromAllAddresses()
                        }
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Error getting default address: ${result.message}, trying all addresses")
                        loadFromAllAddresses()
                    }
                    else -> {
                        loadFromAllAddresses()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading default address", e)
                loadFromAllAddresses()
            }
        }
    }
    
    private suspend fun loadFromAllAddresses() {
        try {
            val result = addressRepository.getAddresses().first { it !is Resource.Loading }
            
            when (result) {
                is Resource.Success -> {
                    val addresses = result.data ?: emptyList()
                    Log.d(TAG, "Loaded ${addresses.size} addresses")
                    val defaultAddress = addresses.find { it.isDefault } ?: addresses.firstOrNull()
                    
                    if (defaultAddress != null) {
                        Log.d(TAG, "Using address: ${defaultAddress.label} - ${defaultAddress.shortAddress}")
                        setAddressFromUserAddress(defaultAddress)
                    } else {
                        Log.d(TAG, "No saved addresses found - will use GPS fallback")
                        currentDeliveryLat = null
                        currentDeliveryLng = null
                        _deliveryAddress.value = null
                        _deliveryLabel.value = null
                        _hasLoadedAddress.value = true
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "Error loading all addresses: ${result.message} - will use GPS fallback")
                    currentDeliveryLat = null
                    currentDeliveryLng = null
                    _deliveryAddress.value = null
                    _hasLoadedAddress.value = true
                }
                else -> {
                    currentDeliveryLat = null
                    currentDeliveryLng = null
                    _deliveryAddress.value = null
                    _hasLoadedAddress.value = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception loading all addresses", e)
            currentDeliveryLat = null
            currentDeliveryLng = null
            _deliveryAddress.value = null
            _hasLoadedAddress.value = true
        }
    }
    
    private fun setAddressFromUserAddress(address: com.example.eco_plate.data.models.UserAddress) {
        Log.d(TAG, "Setting address: ${address.label} - ${address.shortAddress}")
        _deliveryAddress.value = address.shortAddress
        _deliveryLabel.value = address.label
        
        // Set coordinates BEFORE marking as loaded to prevent race condition
        if (address.latitude != null && address.longitude != null) {
            currentDeliveryLat = address.latitude
            currentDeliveryLng = address.longitude
            _hasLoadedAddress.value = true  // Set after coords are ready
            Log.d(TAG, "Loading stores for delivery address: ${address.latitude}, ${address.longitude}")
            loadNearbyStores(address.latitude, address.longitude)
            searchItems(address.latitude, address.longitude)
        } else {
            Log.w(TAG, "Address has no coordinates - will use GPS fallback")
            _hasLoadedAddress.value = true
        }
    }

    fun updateDeliveryAddress(newAddress: String) {
        _deliveryAddress.value = newAddress
    }
    
    /**
     * Update delivery address with coordinates and reload stores
     */
    fun updateDeliveryLocation(address: String, latitude: Double, longitude: Double, label: String? = null) {
        Log.d(TAG, "Updating delivery location: $label - $address ($latitude, $longitude)")
        _deliveryAddress.value = address
        _deliveryLabel.value = label
        currentDeliveryLat = latitude
        currentDeliveryLng = longitude
        
        // Reload stores for new location
        loadNearbyStores(latitude, longitude)
        searchItems(latitude, longitude)
    }
    
    /**
     * Get current delivery coordinates
     */
    fun getCurrentDeliveryLocation(): Pair<Double, Double>? {
        val lat = currentDeliveryLat
        val lng = currentDeliveryLng
        return if (lat != null && lng != null) Pair(lat, lng) else null
    }
}