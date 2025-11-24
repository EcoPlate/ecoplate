package com.example.eco_plate.ui.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.local.TokenManager
import com.example.eco_plate.data.models.Store
import com.example.eco_plate.data.repository.StoreRepository
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _storeState = MutableLiveData<Resource<Store>>(Resource.Loading())
    val storeState: LiveData<Resource<Store>> = _storeState

    private val _myStores = MutableLiveData<Resource<List<Store>>>()
    val myStores: LiveData<Resource<List<Store>>> = _myStores

    private val _currentStore = MutableLiveData<Store?>()
    val currentStore: LiveData<Store?> = _currentStore

    init {
        loadMyStores()
    }

    fun createStore(
        name: String,
        type: String = "RESTAURANT",
        description: String?,
        address: String,
        city: String,
        region: String,
        postalCode: String,
        country: String = "Canada",
        phone: String,
        email: String? = null,
        website: String? = null,
        openTime: String? = "08:00",
        closeTime: String? = "22:00",
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            val storeData = mutableMapOf<String, Any>(
                "name" to name,
                "type" to type,
                "address" to address,
                "city" to city,
                "region" to region,
                "postalCode" to postalCode,
                "country" to country,
                "phone" to phone
            )
            
            if (openTime != null) storeData["openTime"] = openTime
            if (closeTime != null) storeData["closeTime"] = closeTime

            if (description != null) storeData["description"] = description
            if (email != null) storeData["email"] = email
            if (website != null) storeData["website"] = website
            if (imageUrl != null) storeData["imageUrl"] = imageUrl

            storeRepository.createStore(storeData)
                .onEach { result ->
                    _storeState.value = result
                    if (result is Resource.Success) {
                        result.data?.let { store ->
                            _currentStore.value = store
                            saveStoreId(store.id)
                        }
                        loadMyStores() // Refresh the list of stores
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun loadMyStores() {
        viewModelScope.launch {
            storeRepository.getMyStores()
                .onEach { result ->
                    _myStores.value = result
                    if (result is Resource.Success) {
                        // If there's only one store, set it as the current store
                        val stores = result.data
                        if (stores?.size == 1) {
                            _currentStore.value = stores.first()
                            saveStoreId(stores.first().id)
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun selectStore(store: Store) {
        _currentStore.value = store
        saveStoreId(store.id)
    }

    fun getCurrentStoreId(): String? {
        return _currentStore.value?.id ?: getSavedStoreId()
    }

    private fun saveStoreId(storeId: String) {
        viewModelScope.launch {
            tokenManager.saveStoreId(storeId)
        }
    }

    private fun getSavedStoreId(): String? {
        // This would need to be implemented to retrieve from TokenManager
        // For now, return null
        return null
    }

    fun updateStore(storeId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            storeRepository.updateStore(storeId, updates)
                .onEach { result ->
                    _storeState.value = result
                    if (result is Resource.Success) {
                        result.data?.let { store ->
                            _currentStore.value = store
                        }
                        loadMyStores() // Refresh the list of stores
                    }
                }
                .launchIn(viewModelScope)
        }
    }
}
