package com.example.eco_plate.ui.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.Order
import com.example.eco_plate.data.repository.PaymentRepository
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreOrdersViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StoreOrdersViewModel"
    }

    private val _orders = MutableStateFlow<Resource<List<Order>>>(Resource.Loading())
    val orders: StateFlow<Resource<List<Order>>> = _orders.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus: StateFlow<String?> = _selectedStatus.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _updateResult = MutableSharedFlow<Resource<Order>>()
    val updateResult: SharedFlow<Resource<Order>> = _updateResult.asSharedFlow()

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
            
            paymentRepository.getStoreOrders(
                status = _selectedStatus.value,
                page = currentPage
            ).collect { result ->
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
                            _orders.value = Resource.Error(result.message ?: "Failed to load orders")
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

    fun setStatusFilter(status: String?) {
        _selectedStatus.value = status
        loadOrders(refresh = true)
    }

    fun loadNextPage() {
        if (hasMorePages && _orders.value !is Resource.Loading) {
            currentPage++
            loadOrders()
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String, notes: String? = null) {
        viewModelScope.launch {
            paymentRepository.updateOrderStatus(orderId, newStatus, notes).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { updatedOrder ->
                            val currentOrders = (_orders.value as? Resource.Success)?.data ?: emptyList()
                            val updatedOrders = currentOrders.map { order ->
                                if (order.id == orderId) updatedOrder else order
                            }
                            _orders.value = Resource.Success(updatedOrders)
                            _updateResult.emit(Resource.Success(updatedOrder))
                        }
                    }
                    is Resource.Error -> {
                        _updateResult.emit(Resource.Error(result.message ?: "Update failed"))
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun refundOrder(orderId: String, reason: String? = null) {
        viewModelScope.launch {
            paymentRepository.refundOrder(orderId, reason).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { refundData ->
                            val currentOrders = (_orders.value as? Resource.Success)?.data ?: emptyList()
                            val updatedOrders = currentOrders.map { order ->
                                if (order.id == orderId) refundData.order else order
                            }
                            _orders.value = Resource.Success(updatedOrders)
                            _updateResult.emit(Resource.Success(refundData.order))
                            Log.d(TAG, "Order refunded: ${refundData.refundId}")
                        }
                    }
                    is Resource.Error -> {
                        _updateResult.emit(Resource.Error(result.message ?: "Refund failed"))
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun refresh() {
        loadOrders(refresh = true)
    }
}
