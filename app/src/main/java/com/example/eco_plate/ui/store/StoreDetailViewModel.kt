package com.example.eco_plate.ui.store

import androidx.lifecycle.ViewModel
import com.example.eco_plate.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {
    
    val cartItemsCount = cartRepository.cartItemsCount
    
    fun addToCart(
        storeId: String,
        storeName: String,
        productId: String,
        productName: String,
        price: Float,
        quantity: Int = 1,
        imageUrl: String? = null
    ) {
        val cartItem = com.example.eco_plate.data.repository.CartItem(
            id = productId,
            name = productName,
            storeName = storeName,
            price = price,
            originalPrice = null,
            quantity = quantity,
            imageUrl = imageUrl,
            isEcoFriendly = false
        )
        cartRepository.addToCart(cartItem)
    }
}
