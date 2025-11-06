package com.example.eco_plate.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Define CartItem here to avoid circular dependency
data class CartItem(
    val id: String,
    val name: String,
    val storeName: String,
    val price: Float,
    val originalPrice: Float? = null,
    val quantity: Int,
    val imageUrl: String? = null,
    val isEcoFriendly: Boolean = false,
    val expiryDate: String? = null
)

data class CartStore(
    val storeId: String,
    val storeName: String,
    val storeImage: String? = null,
    val deliveryFee: Float = 2.99f,
    val deliveryTime: String = "20-30 min",
    val items: List<CartItem> = emptyList()
)

@Singleton
class CartRepository @Inject constructor() {
    private val _cartStores = MutableStateFlow<List<CartStore>>(emptyList())
    val cartStores: StateFlow<List<CartStore>> = _cartStores.asStateFlow()
    
    private val _cartItemsCount = MutableStateFlow(0)
    val cartItemsCount: StateFlow<Int> = _cartItemsCount.asStateFlow()
    
    fun addToCart(product: CartItem) {
        val currentStores = _cartStores.value.toMutableList()
        val existingStoreIndex = currentStores.indexOfFirst { it.storeName == product.storeName }

        if (existingStoreIndex != -1) {
            // Store exists, check for item
            val existingStore = currentStores[existingStoreIndex]
            val existingItemIndex = existingStore.items.indexOfFirst { it.id == product.id }

            if (existingItemIndex != -1) {
                // Item exists, update quantity
                val updatedItems = existingStore.items.toMutableList()
                val existingItem = updatedItems[existingItemIndex]
                updatedItems[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + product.quantity)
                currentStores[existingStoreIndex] = existingStore.copy(items = updatedItems)
            } else {
                // Item does not exist in store, add new item
                val updatedItems = existingStore.items.toMutableList()
                updatedItems.add(product)
                currentStores[existingStoreIndex] = existingStore.copy(items = updatedItems)
            }
        } else {
            // Store does not exist, add new store with item
            currentStores.add(
                CartStore(
                    storeId = product.storeName.hashCode().toString(), // Simple ID for demo
                    storeName = product.storeName,
                    items = listOf(product)
                )
            )
        }
        _cartStores.value = currentStores
        updateCartCount()
    }

    
    fun updateQuantity(storeId: String, itemId: String, quantity: Int) {
        val currentStores = _cartStores.value.toMutableList()
        val storeIndex = currentStores.indexOfFirst { it.storeId == storeId }
        
        if (storeIndex != -1) {
            val store = currentStores[storeIndex]
            val updatedItems = store.items.toMutableList()
            val itemIndex = updatedItems.indexOfFirst { it.id == itemId }
            
            if (itemIndex != -1) {
                if (quantity <= 0) {
                    // Remove item
                    updatedItems.removeAt(itemIndex)
                } else {
                    // Update quantity
                    updatedItems[itemIndex] = updatedItems[itemIndex].copy(quantity = quantity)
                }
                
                if (updatedItems.isEmpty()) {
                    // Remove store if no items left
                    currentStores.removeAt(storeIndex)
                } else {
                    currentStores[storeIndex] = store.copy(items = updatedItems)
                }
                
                _cartStores.value = currentStores
                updateCartCount()
            }
        }
    }
    
    fun removeFromCart(storeId: String, itemId: String) {
        updateQuantity(storeId, itemId, 0)
    }
    
    fun clearCart() {
        _cartStores.value = emptyList()
        _cartItemsCount.value = 0
    }
    
    fun clearStoreItems(storeId: String) {
        val currentStores = _cartStores.value.toMutableList()
        currentStores.removeAll { it.storeId == storeId }
        _cartStores.value = currentStores
        updateCartCount()
    }
    
    fun getTotalForStore(storeId: String): Float {
        val store = _cartStores.value.find { it.storeId == storeId }
        return store?.let { 
            it.items.sumOf { item -> (item.price * item.quantity).toDouble() }.toFloat() + it.deliveryFee
        } ?: 0f
    }
    
    fun getCartTotal(): Float {
        return _cartStores.value.sumOf { store ->
            store.items.sumOf { item -> (item.price * item.quantity).toDouble() } + store.deliveryFee
        }.toFloat()
    }
    
    private fun updateCartCount() {
        _cartItemsCount.value = _cartStores.value.sumOf { store ->
            store.items.sumOf { it.quantity }
        }
    }
}