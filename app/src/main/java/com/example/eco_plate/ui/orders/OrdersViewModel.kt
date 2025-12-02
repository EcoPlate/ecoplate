package com.example.eco_plate.ui.orders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.repository.OrderRepository
import com.example.eco_plate.data.models.Order
import com.example.eco_plate.utils.Resource
import com.example.eco_plate.widget.ordersToWidgetLines
import com.example.eco_plate.widget.updateNotificationWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _orders = MutableStateFlow<Resource<List<Order>>>(Resource.Loading())
    val orders: StateFlow<Resource<List<Order>>> = _orders.asStateFlow()
    // Alias for backward compatibility with ModernOrdersScreen
    val ordersState: StateFlow<Resource<List<Order>>> = _orders.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedOrder = MutableStateFlow<Resource<Order>?>(null)
    val selectedOrder: StateFlow<Resource<Order>?> = _selectedOrder.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadOrders()
    }

    fun loadOrders() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _orders.value = Resource.Loading()
            orderRepository.getMyOrders().collect { res ->
                _orders.value = res
                _isRefreshing.value = false

                if (res is Resource.Success && res.data != null) {
                    val lines = ordersToWidgetLines(res.data)
                    updateNotificationWidget(appContext, lines)
                }
            }
        }
    }

    fun refresh() {
        _isRefreshing.value = true
        loadOrders()
    }

    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            _selectedOrder.value = Resource.Loading()
            orderRepository.getOrder(orderId).collect { res ->
                _selectedOrder.value = res
            }
        }
    }

    fun clearSelectedOrder() {
        _selectedOrder.value = null
    }
}
