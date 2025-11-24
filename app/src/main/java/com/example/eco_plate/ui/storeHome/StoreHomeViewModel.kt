package com.example.eco_plate.ui.storeHome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.local.TokenManager
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.data.models.Store
import com.example.eco_plate.data.repository.CartItem
import com.example.eco_plate.data.repository.CartRepository
import com.example.eco_plate.data.repository.InventoryRepository
import com.example.eco_plate.data.repository.StoreRepository
import com.example.eco_plate.ui.search.SearchProduct
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreHomeViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val storeRepository: StoreRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _storeItems = MutableLiveData<Resource<List<Item>>>()
    val storeItems: LiveData<Resource<List<Item>>> = _storeItems
    
    private val _currentStore = MutableLiveData<Resource<Store>>()
    val currentStore: LiveData<Resource<Store>> = _currentStore
    
    private val _storeId = MutableLiveData<String?>()
    val storeId: LiveData<String?> = _storeId
    
    init {
        loadStoreId()
    }
    
    private fun loadStoreId() {
        viewModelScope.launch {
            // First check if we have a saved store ID
            val savedStoreId = tokenManager.storeId.first()
            if (savedStoreId != null) {
                _storeId.value = savedStoreId
                loadStoreData(savedStoreId)
                loadStoreItems(savedStoreId)
            } else {
                // If not, load the user's stores
                storeRepository.getMyStores()
                    .onEach { result ->
                        if (result is Resource.Success) {
                            val stores = result.data
                            if (!stores.isNullOrEmpty()) {
                                // Use the first store
                                val firstStore = stores.first()
                                _storeId.value = firstStore.id
                                tokenManager.saveStoreId(firstStore.id)
                                loadStoreData(firstStore.id)
                                loadStoreItems(firstStore.id)
                            }
                        }
                    }
                    .launchIn(viewModelScope)
            }
        }
    }
    
    fun loadStoreData(storeId: String) {
        viewModelScope.launch {
            storeRepository.getStore(storeId)
                .onEach { result ->
                    _currentStore.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun loadStoreItems(
        storeId: String,
        category: String? = null,
        available: Boolean? = null
    ) {
        viewModelScope.launch {
            inventoryRepository.getStoreItems(
                storeId = storeId,
                category = category,
                available = available,
                limit = 100
            ).onEach { result ->
                _storeItems.value = result
            }.launchIn(viewModelScope)
        }
    }
    
    fun refreshStore() {
        _storeId.value?.let { id ->
            loadStoreData(id)
            loadStoreItems(id)
        }
    }
    
    fun updateItemAvailability(itemId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            inventoryRepository.updateItemAvailability(itemId, isAvailable)
                .onEach { result ->
                    if (result is Resource.Success) {
                        // Refresh the items list
                        _storeId.value?.let { loadStoreItems(it) }
                    }
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun updateItemQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch {
            inventoryRepository.updateItemQuantity(itemId, quantity)
                .onEach { result ->
                    if (result is Resource.Success) {
                        // Refresh the items list
                        _storeId.value?.let { loadStoreItems(it) }
                    }
                }
                .launchIn(viewModelScope)
        }
    }
}