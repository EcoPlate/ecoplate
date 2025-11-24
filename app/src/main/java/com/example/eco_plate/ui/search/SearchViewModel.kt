package com.example.eco_plate.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.SearchItemsResponse
import com.example.eco_plate.data.models.SearchStoresResponse
import com.example.eco_plate.data.models.Store
import com.example.eco_plate.data.repository.SearchRepository
import com.example.eco_plate.ui.location.LocationManager
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val locationManager: LocationManager
) : ViewModel() {
    
    private val _itemSearchResults = MutableLiveData<Resource<SearchItemsResponse>>()
    val itemSearchResults: LiveData<Resource<SearchItemsResponse>> = _itemSearchResults
    
    private val _storeSearchResults = MutableLiveData<Resource<SearchStoresResponse>>()
    val storeSearchResults: LiveData<Resource<SearchStoresResponse>> = _storeSearchResults
    
    private val _nearbyStores = MutableLiveData<Resource<List<Store>>>()
    val nearbyStores: LiveData<Resource<List<Store>>> = _nearbyStores
    
    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery
    
    fun searchItems(
        query: String? = null,
        category: String? = null,
        radius: Double? = 5.0,
        minDiscount: Int? = null,
        maxPrice: Double? = null,
        nearBestBefore: Boolean? = null,
        isClearance: Boolean? = null,
        storeType: String? = null,
        limit: Int? = 20,
        offset: Int? = 0
    ) {
        _searchQuery.value = query ?: ""
        
        locationManager.getLastKnownLocation { location ->
            if (location == null) {
                _itemSearchResults.value = Resource.Error("Location not available. Please enable location services.")
                return@getLastKnownLocation
            }
            
            viewModelScope.launch {
                searchRepository.searchItems(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = radius,
                    category = category,
                    query = query,
                    minDiscount = minDiscount,
                    maxPrice = maxPrice,
                    nearBestBefore = nearBestBefore,
                    isClearance = isClearance,
                    storeType = storeType,
                    limit = limit,
                    offset = offset
                ).onEach { result ->
                    _itemSearchResults.value = result
                }.launchIn(viewModelScope)
            }
        }
    }
    
    fun searchStores(
        query: String? = null,
        category: String? = null,
        radius: Double? = 5.0,
        limit: Int? = 20,
        offset: Int? = 0
    ) {
        _searchQuery.value = query ?: ""
        
        locationManager.getLastKnownLocation { location ->
            if (location == null) {
                _storeSearchResults.value = Resource.Error("Location not available. Please enable location services.")
                return@getLastKnownLocation
            }
            
            viewModelScope.launch {
                searchRepository.searchStores(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = radius,
                    category = category,
                    query = query,
                    limit = limit,
                    offset = offset
                ).onEach { result ->
                    _storeSearchResults.value = result
                }.launchIn(viewModelScope)
            }
        }
    }
    
    fun getNearbyStores(limit: Int? = 10) {
        locationManager.getLastKnownLocation { location ->
            if (location == null) {
                _nearbyStores.value = Resource.Error("Location not available. Please enable location services.")
                return@getLastKnownLocation
            }
            
            viewModelScope.launch {
                searchRepository.getNearbyStores(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    limit = limit
                ).onEach { result ->
                    _nearbyStores.value = result
                }.launchIn(viewModelScope)
            }
        }
    }
    
    fun clearSearchResults() {
        _searchQuery.value = ""
        _itemSearchResults.value = Resource.Success(SearchItemsResponse(
            data = emptyList(),
            total = 0,
            skip = 0,
            take = 20
        ))
        _storeSearchResults.value = Resource.Success(SearchStoresResponse(
            data = emptyList(),
            total = 0,
            skip = 0,
            take = 20
        ))
    }
}