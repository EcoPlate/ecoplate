package com.example.eco_plate.ui.cart

import androidx.lifecycle.ViewModel
import com.example.eco_plate.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {
    
    val cartStores = cartRepository.cartStores
    val cartItemsCount = cartRepository.cartItemsCount
    
    fun updateQuantity(storeId: String, itemId: String, quantity: Int) {
        cartRepository.updateQuantity(storeId, itemId, quantity)
    }
    
    fun removeFromCart(storeId: String, itemId: String) {
        cartRepository.removeFromCart(storeId, itemId)
    }
    
    fun clearCart() {
        cartRepository.clearCart()
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
}