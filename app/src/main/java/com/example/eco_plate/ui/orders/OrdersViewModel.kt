package com.example.eco_plate.ui.orders

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is orders Fragment"
    }
    val text: LiveData<String> = _text

    private val _ordersState = MutableStateFlow<Resource<List<Order>>>(Resource.Loading())
    val ordersState: StateFlow<Resource<List<Order>>> = _ordersState

    private var loadJob: Job? = null

    fun loadOrders() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            orderRepository.getMyOrders().collect { res ->
                _ordersState.value = res

                if (res is Resource.Success && res.data != null) {
                    val lines = ordersToWidgetLines(res.data)
                    updateNotificationWidget(appContext, lines)
                }
            }
        }
    }
}
