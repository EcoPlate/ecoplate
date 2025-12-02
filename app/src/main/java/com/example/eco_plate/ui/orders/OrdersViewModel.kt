package com.example.eco_plate.ui.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.Order
import com.example.eco_plate.data.models.OrdersResponse
import com.example.eco_plate.data.repository.PaymentRepository
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "OrdersViewModel"
    }

    private val _orders = MutableStateFlow<Resource<List<Order>>>(Resource.Loading())
    val orders: StateFlow<Resource<List<Order>>> = _orders.asStateFlow()
    
    // Alias for ModernOrdersScreen compatibility
    val ordersState: StateFlow<Resource<List<Order>>> = _orders.asStateFlow()

    private val _selectedOrder = MutableStateFlow<Resource<Order>?>(null)
    val selectedOrder: StateFlow<Resource<Order>?> = _selectedOrder.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var currentPage = 1
    private var hasMorePages = true

    init {
        loadOrders()
    }

    fun loadOrders(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 1
            hasMorePages = true
        }

        viewModelScope.launch {
            if (refresh) _isRefreshing.value = true
            
            paymentRepository.getMyOrders(page = currentPage).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { ordersData ->
                            if (refresh || currentPage == 1) {
                                _orders.value = Resource.Success(ordersData.data)
                            } else {
                                val currentOrders = (_orders.value as? Resource.Success)?.data ?: emptyList()
                                _orders.value = Resource.Success(currentOrders + ordersData.data)
                            }
                            hasMorePages = currentPage < ordersData.totalPages
                        }
                    }
                    is Resource.Error -> {
                        if (currentPage == 1) {
                            _orders.value = Resource.Error(result.message ?: "Error loading orders")
                        }
                    }
                    is Resource.Loading -> {
                        if (currentPage == 1) {
                            _orders.value = Resource.Loading()
                        }
                    }
                }
                _isRefreshing.value = false
            }
        }
    }

    fun loadNextPage() {
        if (hasMorePages && _orders.value !is Resource.Loading) {
            currentPage++
            loadOrders()
        }
    }

    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            paymentRepository.getOrder(orderId).collect { result ->
                _selectedOrder.value = result
            }
        }
    }

    fun clearSelectedOrder() {
        _selectedOrder.value = null
    }

    fun refresh() {
        loadOrders(refresh = true)
    }
}
