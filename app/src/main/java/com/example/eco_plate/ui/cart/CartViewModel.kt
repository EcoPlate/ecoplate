package com.example.eco_plate.ui.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.Cart
import com.example.eco_plate.data.repository.CartRepository
import com.example.eco_plate.data.repository.CartItem
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CartViewModel"
    }
    
    val cartStores = cartRepository.cartStores
    val cartItemsCount = cartRepository.cartItemsCount
    val isLoading = cartRepository.isLoading
    val backendCart = cartRepository.backendCart

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            cartRepository.loadCart().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "Cart loaded: ${result.data?.items?.size} items")
                        _error.value = null
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Error loading cart: ${result.message}")
                        _error.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun addToCart(itemId: String, quantity: Int = 1) {
        viewModelScope.launch {
            cartRepository.addToCart(itemId, quantity).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "Item added to cart")
                        _error.value = null
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Error adding to cart: ${result.message}")
                        _error.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun updateQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(itemId, quantity).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _error.value = null
                    }
                    is Resource.Error -> {
                        _error.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    // Legacy method signature for backwards compatibility
    fun updateQuantity(storeId: String, itemId: String, quantity: Int) {
        updateQuantity(itemId, quantity)
    }
    
    fun removeFromCart(itemId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(itemId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _error.value = null
                    }
                    is Resource.Error -> {
                        _error.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    // Legacy method signature for backwards compatibility
    fun removeFromCart(storeId: String, itemId: String) {
        removeFromCart(itemId)
    }
    
    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _error.value = null
                    }
                    is Resource.Error -> {
                        _error.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }
    
    fun clearStoreItems(storeId: String) {
        cartRepository.clearStoreItems(storeId)
    }
    
    fun getTotalForStore(storeId: String): Float {
        return cartRepository.getTotalForStore(storeId)
    }
    
    fun getCartTotal(): Float {
        return cartRepository.getCartTotal()
    }

    fun getCartSubtotal(): Float {
        return cartRepository.getCartSubtotal()
    }

    fun getCartTax(): Float {
        return cartRepository.getCartTax()
    }

    fun clearError() {
        _error.value = null
    }
}
