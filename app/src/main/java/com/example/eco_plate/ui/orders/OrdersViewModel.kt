package com.example.eco_plate.ui.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eco_plate.data.repository.CartRepository
import com.example.eco_plate.data.repository.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is orders Fragment"
    }
    val text: LiveData<String> = _text
    
    fun reorderItems(order: Order) {
        // Convert each order item to cart item and add to cart
        order.items.forEach { orderItem ->
            val cartItem = CartItem(
                id = "${order.id}_${orderItem.name}", // Generate unique ID
                name = orderItem.name,
                storeName = order.storeName,
                price = orderItem.price,
                originalPrice = null,
                quantity = orderItem.quantity,
                imageUrl = orderItem.image,
                isEcoFriendly = false,
                expiryDate = null
            )
            cartRepository.addToCart(cartItem)
        }
    }
}
