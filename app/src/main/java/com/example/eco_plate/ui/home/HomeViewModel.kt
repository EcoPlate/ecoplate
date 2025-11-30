package com.example.eco_plate.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.Store
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.data.repository.CartRepository
import com.example.eco_plate.data.repository.SearchRepository
import com.example.eco_plate.data.repository.StoreRepository
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val storeRepository: StoreRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _nearbyStores = MutableLiveData<Resource<List<Store>>>()
    val nearbyStores: LiveData<Resource<List<Store>>> = _nearbyStores

    private val _featuredItems = MutableLiveData<Resource<List<Item>>>()
    val featuredItems: LiveData<Resource<List<Item>>> = _featuredItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadNearbyStores(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            searchRepository.getNearbyStores(latitude, longitude, 10)
                .onEach { result ->
                    _nearbyStores.value = result
                    _isLoading.value = result is Resource.Loading
                }
                .launchIn(viewModelScope)
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
            searchRepository.searchItems(
                latitude = latitude,
                longitude = longitude,
                category = category,
                query = query,
                minDiscount = minDiscount,  // Don't force discount filter
                limit = limit  // Pass limit to get more products
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
        val cartItem = com.example.eco_plate.data.repository.CartItem(
            id = productId,
            name = productName,
            storeName = storeName,
            price = price,
            originalPrice = null,
            quantity = quantity,
            imageUrl = imageUrl,
            isEcoFriendly = isEcoFriendly
        )
        cartRepository.addToCart(cartItem)
    }

    //Change Delivery Address Value on Home page.
    //Doesn't change the users address in the backend.
    private val _deliveryAddress = MutableStateFlow("123 Main Street, Vancouver")
    val deliveryAddress = _deliveryAddress.asStateFlow()

    // --- ADD THIS FUNCTION TO UPDATE THE ADDRESS ---
    fun updateDeliveryAddress(newAddress: String) {
        _deliveryAddress.value = newAddress
    }
}