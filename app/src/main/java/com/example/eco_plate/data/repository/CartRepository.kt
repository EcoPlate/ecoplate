package com.example.eco_plate.data.repository

import android.util.Log
import com.example.eco_plate.data.api.CartApi
import com.example.eco_plate.data.models.AddToCartRequest
import com.example.eco_plate.data.models.Cart
import com.example.eco_plate.data.models.UpdateCartItemRequest
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

// Local cart item for UI compatibility
data class CartItem(
    val id: String,
    val name: String,
    val storeName: String,
    val storeId: String = "",
    val price: Float,
    val originalPrice: Float? = null,
    val quantity: Int,
    val imageUrl: String? = null,
    val isEcoFriendly: Boolean = false,
    val expiryDate: String? = null,
    val itemId: String = "" // Backend item ID
)

data class CartStore(
    val storeId: String,
    val storeName: String,
    val storeImage: String? = null,
    val deliveryFee: Float = 0f,
    val deliveryTime: String = "20-30 min",
    val items: List<CartItem> = emptyList()
)

@Singleton
class CartRepository @Inject constructor(
    private val cartApi: CartApi
) {
    companion object {
        private const val TAG = "CartRepository"
    }

    private val _cartStores = MutableStateFlow<List<CartStore>>(emptyList())
    val cartStores: StateFlow<List<CartStore>> = _cartStores.asStateFlow()
    
    private val _cartItemsCount = MutableStateFlow(0)
    val cartItemsCount: StateFlow<Int> = _cartItemsCount.asStateFlow()

    private val _backendCart = MutableStateFlow<Cart?>(null)
    val backendCart: StateFlow<Cart?> = _backendCart.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Load cart from backend
     */
    fun loadCart(): Flow<Resource<Cart>> = flow {
        emit(Resource.Loading())
        _isLoading.value = true
        try {
            Log.d(TAG, "Loading cart from backend...")
            val response = cartApi.getCart()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val cart = apiResponse.data
                    _backendCart.value = cart
                    updateLocalCartFromBackend(cart)
                    Log.d(TAG, "Cart loaded: ${cart.items.size} items, total: ${cart.total}")
                    emit(Resource.Success(cart))
                } else {
                    Log.e(TAG, "API error: ${apiResponse?.message ?: "Unknown error"}")
                    emit(Resource.Error(apiResponse?.message ?: "Failed to load cart"))
                }
            } else {
                Log.e(TAG, "Failed to load cart: ${response.code()} - ${response.errorBody()?.string()}")
                emit(Resource.Error("Failed to load cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cart", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Add item to cart via backend
     */
    fun addToCart(itemId: String, quantity: Int = 1): Flow<Resource<Cart>> = flow {
        emit(Resource.Loading())
        try {
            Log.d(TAG, "Adding item $itemId to cart (qty: $quantity)")
            val response = cartApi.addToCart(AddToCartRequest(itemId, quantity))
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val cart = apiResponse.data
                    _backendCart.value = cart
                    updateLocalCartFromBackend(cart)
                    Log.d(TAG, "Added item $itemId to cart, total items: ${cart.items.size}")
                    emit(Resource.Success(cart))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to add to cart"))
                }
            } else {
                Log.e(TAG, "Failed to add to cart: ${response.code()} - ${response.errorBody()?.string()}")
                emit(Resource.Error("Failed to add to cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to cart", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    /**
     * Update cart item quantity
     */
    fun updateQuantity(itemId: String, quantity: Int): Flow<Resource<Cart>> = flow {
        emit(Resource.Loading())
        try {
            Log.d(TAG, "Updating cart item $itemId to qty: $quantity")
            val response = cartApi.updateCartItem(itemId, UpdateCartItemRequest(quantity))
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val cart = apiResponse.data
                    _backendCart.value = cart
                    updateLocalCartFromBackend(cart)
                    emit(Resource.Success(cart))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to update cart"))
                }
            } else {
                Log.e(TAG, "Failed to update cart: ${response.code()}")
                emit(Resource.Error("Failed to update cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating cart", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    /**
     * Remove item from cart
     */
    fun removeFromCart(itemId: String): Flow<Resource<Cart>> = flow {
        emit(Resource.Loading())
        try {
            Log.d(TAG, "Removing item $itemId from cart")
            val response = cartApi.removeFromCart(itemId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val cart = apiResponse.data
                    _backendCart.value = cart
                    updateLocalCartFromBackend(cart)
                    Log.d(TAG, "Removed item, cart now has ${cart.items.size} items")
                    emit(Resource.Success(cart))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Failed to remove from cart"))
                }
            } else {
                Log.e(TAG, "Failed to remove from cart: ${response.code()}")
                emit(Resource.Error("Failed to remove from cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from cart", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    /**
     * Clear entire cart
     */
    fun clearCart(): Flow<Resource<Cart>> = flow {
        emit(Resource.Loading())
        try {
            Log.d(TAG, "Clearing cart")
            val response = cartApi.clearCart()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val cart = apiResponse.data
                    _backendCart.value = cart
                    updateLocalCartFromBackend(cart)
                    emit(Resource.Success(cart))
                } else {
                    _cartStores.value = emptyList()
                    _cartItemsCount.value = 0
                    _backendCart.value = null
                    emit(Resource.Success(Cart("", null, 0.0, 0.0, 0.0, 0.0, emptyList())))
                }
            } else {
                Log.e(TAG, "Failed to clear cart: ${response.code()}")
                emit(Resource.Error("Failed to clear cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cart", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    /**
     * Convert backend cart to local format for UI
     */
    private fun updateLocalCartFromBackend(cart: Cart) {
        val storeMap = mutableMapOf<String, MutableList<CartItem>>()
        val storeNames = mutableMapOf<String, String>()

        for (cartItem in cart.items) {
            val item = cartItem.item ?: continue
            val store = item.store ?: continue
            
            val storeId = store.id
            storeNames[storeId] = store.name

            val localItem = CartItem(
                id = cartItem.id,
                name = item.name,
                storeName = store.name,
                storeId = storeId,
                price = cartItem.price.toFloat(),
                originalPrice = item.originalPrice.toFloat(),
                quantity = cartItem.quantity,
                imageUrl = item.images.firstOrNull(),
                isEcoFriendly = true,
                expiryDate = null,
                itemId = cartItem.itemId
            )

            storeMap.getOrPut(storeId) { mutableListOf() }.add(localItem)
        }

        val stores = storeMap.map { (storeId, items) ->
            CartStore(
                storeId = storeId,
                storeName = storeNames[storeId] ?: "Store",
                items = items
            )
        }

        _cartStores.value = stores
        _cartItemsCount.value = cart.items.sumOf { it.quantity }
    }

    // Legacy methods for local-only operations (backwards compatibility)
    
    /**
     * Add to cart with CartItem object (legacy method for local operations)
     * This supports both backend and local operations
     */
    fun addToCart(product: CartItem): Flow<Resource<Cart>>? {
        // If itemId is present, use backend
        if (product.itemId.isNotEmpty()) {
            return addToCart(product.itemId, product.quantity)
        }
        // Otherwise, add locally
        addToCartLocal(product)
        return null
    }
    
    fun addToCartLocal(product: CartItem) {
        val currentStores = _cartStores.value.toMutableList()
        val existingStoreIndex = currentStores.indexOfFirst { it.storeName == product.storeName }

        if (existingStoreIndex != -1) {
            val existingStore = currentStores[existingStoreIndex]
            val existingItemIndex = existingStore.items.indexOfFirst { it.id == product.id }

            if (existingItemIndex != -1) {
                val updatedItems = existingStore.items.toMutableList()
                val existingItem = updatedItems[existingItemIndex]
                updatedItems[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + product.quantity)
                currentStores[existingStoreIndex] = existingStore.copy(items = updatedItems)
            } else {
                val updatedItems = existingStore.items.toMutableList()
                updatedItems.add(product)
                currentStores[existingStoreIndex] = existingStore.copy(items = updatedItems)
            }
        } else {
            currentStores.add(
                CartStore(
                    storeId = product.storeId.ifEmpty { product.storeName.hashCode().toString() },
                    storeName = product.storeName,
                    items = listOf(product)
                )
            )
        }
        _cartStores.value = currentStores
        updateCartCount()
    }
    
    fun updateQuantityLocal(storeId: String, itemId: String, quantity: Int) {
        val currentStores = _cartStores.value.toMutableList()
        val storeIndex = currentStores.indexOfFirst { it.storeId == storeId }
        
        if (storeIndex != -1) {
            val store = currentStores[storeIndex]
            val updatedItems = store.items.toMutableList()
            val itemIndex = updatedItems.indexOfFirst { it.id == itemId }
            
            if (itemIndex != -1) {
                if (quantity <= 0) {
                    updatedItems.removeAt(itemIndex)
                } else {
                    updatedItems[itemIndex] = updatedItems[itemIndex].copy(quantity = quantity)
                }
                
                if (updatedItems.isEmpty()) {
                    currentStores.removeAt(storeIndex)
                } else {
                    currentStores[storeIndex] = store.copy(items = updatedItems)
                }
                
                _cartStores.value = currentStores
                updateCartCount()
            }
        }
    }
    
    fun removeFromCartLocal(storeId: String, itemId: String) {
        updateQuantityLocal(storeId, itemId, 0)
    }
    
    fun clearCartLocal() {
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
        return _backendCart.value?.total?.toFloat() 
            ?: _cartStores.value.sumOf { store ->
                store.items.sumOf { item -> (item.price * item.quantity).toDouble() } + store.deliveryFee
            }.toFloat()
    }

    fun getCartSubtotal(): Float {
        return _backendCart.value?.subtotal?.toFloat()
            ?: _cartStores.value.sumOf { store ->
                store.items.sumOf { item -> (item.price * item.quantity).toDouble() }
            }.toFloat()
    }

    fun getCartTax(): Float {
        return _backendCart.value?.tax?.toFloat() ?: (getCartSubtotal() * 0.12f)
    }
    
    private fun updateCartCount() {
        _cartItemsCount.value = _cartStores.value.sumOf { store ->
            store.items.sumOf { it.quantity }
        }
    }
}
