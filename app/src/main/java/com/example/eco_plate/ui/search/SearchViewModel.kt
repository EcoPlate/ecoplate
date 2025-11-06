package com.example.eco_plate.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.repository.CartItem
import com.example.eco_plate.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<Any>>(emptyList())
    val searchResults: StateFlow<List<Any>> = _searchResults.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    val cartItemsCount = cartRepository.cartItemsCount
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        performSearch(query)
    }
    
    private fun performSearch(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // TODO: Implement actual search logic here
            // For now, we'll just simulate a search
            kotlinx.coroutines.delay(500)
            _searchResults.value = if (query.isNotEmpty()) {
                // Return mock results
                listOf("Result 1", "Result 2", "Result 3")
            } else {
                emptyList()
            }
            _isLoading.value = false
        }
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
    
    fun addToCart(product: SearchProduct) {
        val cartItem = CartItem(
            id = product.id,
            name = product.name,
            storeName = product.store,
            price = product.price,
            originalPrice = product.originalPrice,
            quantity = 1,
            imageUrl = product.imageUrl,
            isEcoFriendly = product.isEcoFriendly,
            expiryDate = null
        )
        cartRepository.addToCart(cartItem)
    }
}