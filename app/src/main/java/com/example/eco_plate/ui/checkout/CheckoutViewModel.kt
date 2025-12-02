package com.example.eco_plate.ui.checkout

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.BuildConfig
import com.example.eco_plate.data.models.*
import com.example.eco_plate.data.repository.PaymentRepository
import com.example.eco_plate.utils.Resource
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    application: Application,
    private val paymentRepository: PaymentRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CheckoutViewModel"
    }

    private val _cart = MutableStateFlow<Resource<Cart>>(Resource.Loading())
    val cart: StateFlow<Resource<Cart>> = _cart.asStateFlow()

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private var currentPaymentIntentId: String? = null
    private var currentClientSecret: String? = null

    private val _tipAmount = MutableStateFlow(0.0)
    val tipAmount: StateFlow<Double> = _tipAmount.asStateFlow()

    private val _customerNotes = MutableStateFlow("")
    val customerNotes: StateFlow<String> = _customerNotes.asStateFlow()

    private val _selectedAddressId = MutableStateFlow<String?>(null)
    val selectedAddressId: StateFlow<String?> = _selectedAddressId.asStateFlow()

    init {
        PaymentConfiguration.init(
            application,
            BuildConfig.STRIPE_PUBLISHABLE_KEY
        )
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            paymentRepository.getCart().collect { result ->
                _cart.value = result
            }
        }
    }

    fun addToCart(itemId: String, quantity: Int = 1) {
        viewModelScope.launch {
            paymentRepository.addToCart(itemId, quantity).collect { result ->
                _cart.value = result
            }
        }
    }

    fun updateCartItem(itemId: String, quantity: Int) {
        viewModelScope.launch {
            paymentRepository.updateCartItem(itemId, quantity).collect { result ->
                _cart.value = result
            }
        }
    }

    fun removeFromCart(itemId: String) {
        viewModelScope.launch {
            paymentRepository.removeFromCart(itemId).collect { result ->
                _cart.value = result
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            paymentRepository.clearCart().collect { result ->
                _cart.value = result
            }
        }
    }

    fun setTipAmount(amount: Double) {
        _tipAmount.value = amount
    }

    fun setCustomerNotes(notes: String) {
        _customerNotes.value = notes
    }

    fun setSelectedAddress(addressId: String?) {
        _selectedAddressId.value = addressId
    }

    fun createPaymentIntent() {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            
            paymentRepository.createPaymentIntent(
                addressId = _selectedAddressId.value,
                tip = _tipAmount.value
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { data ->
                            currentPaymentIntentId = data.paymentIntentId
                            currentClientSecret = data.clientSecret
                            _paymentState.value = PaymentState.ReadyToPay(
                                clientSecret = data.clientSecret,
                                amount = data.amount
                            )
                            Log.d(TAG, "Payment intent ready: ${data.paymentIntentId}")
                        } ?: run {
                            _paymentState.value = PaymentState.Error("Failed to create payment")
                        }
                    }
                    is Resource.Error -> {
                        _paymentState.value = PaymentState.Error(result.message ?: "Failed to create payment")
                    }
                    is Resource.Loading -> {
                        _paymentState.value = PaymentState.Loading
                    }
                }
            }
        }
    }

    fun getPaymentSheetConfiguration(): PaymentSheet.Configuration {
        return PaymentSheet.Configuration.Builder("EcoPlate")
            .allowsDelayedPaymentMethods(false)
            .build()
    }

    fun handlePaymentResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                Log.d(TAG, "Payment completed successfully")
                confirmPaymentOnBackend()
            }
            is PaymentSheetResult.Canceled -> {
                Log.d(TAG, "Payment canceled")
                _paymentState.value = PaymentState.Canceled
            }
            is PaymentSheetResult.Failed -> {
                Log.e(TAG, "Payment failed: ${result.error.message}")
                _paymentState.value = PaymentState.Error(result.error.message ?: "Payment failed")
            }
        }
    }

    private fun confirmPaymentOnBackend() {
        val paymentIntentId = currentPaymentIntentId
        if (paymentIntentId == null) {
            _paymentState.value = PaymentState.Error("Payment intent not found")
            return
        }

        viewModelScope.launch {
            _paymentState.value = PaymentState.Processing
            
            paymentRepository.confirmPayment(
                paymentIntentId = paymentIntentId,
                addressId = _selectedAddressId.value,
                tip = _tipAmount.value,
                customerNotes = _customerNotes.value.takeIf { it.isNotBlank() }
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { data ->
                            _orders.value = data.orders
                            _paymentState.value = PaymentState.Success(data.orders)
                            Log.d(TAG, "Orders created: ${data.orders.map { it.orderNumber }}")
                            loadCart()
                        } ?: run {
                            _paymentState.value = PaymentState.Error("Failed to confirm payment")
                        }
                    }
                    is Resource.Error -> {
                        _paymentState.value = PaymentState.Error(result.message ?: "Failed to confirm payment")
                    }
                    is Resource.Loading -> {
                        _paymentState.value = PaymentState.Processing
                    }
                }
            }
        }
    }

    fun resetPaymentState() {
        _paymentState.value = PaymentState.Idle
        currentPaymentIntentId = null
        currentClientSecret = null
    }
}

sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    data class ReadyToPay(val clientSecret: String, val amount: Double) : PaymentState()
    object Processing : PaymentState()
    data class Success(val orders: List<Order>) : PaymentState()
    object Canceled : PaymentState()
    data class Error(val message: String) : PaymentState()
}
