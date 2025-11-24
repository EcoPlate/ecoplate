package com.example.eco_plate.ui.foodFeed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.data.models.SearchItemsResponse
import com.example.eco_plate.data.models.Store
import com.example.eco_plate.data.repository.SearchRepository
import com.example.eco_plate.ui.location.LocationManager
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodFeedViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val locationManager: LocationManager
) : ViewModel() {
    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    // Feed items (products near user)
    private val _feedItems = MutableLiveData<Resource<SearchItemsResponse>>()
    val feedItems: LiveData<Resource<SearchItemsResponse>> = _feedItems
    
    // Nearby stores
    private val _nearbyStores = MutableLiveData<Resource<List<Store>>>()
    val nearbyStores: LiveData<Resource<List<Store>>> = _nearbyStores
    
    init {
        loadFeedItems()
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun loadFeedItems() {
        locationManager.getLastKnownLocation { location ->
            if (location == null) {
                _feedItems.value = Resource.Error("Location not available. Please enable location services.")
                return@getLastKnownLocation
            }
            
            viewModelScope.launch {
                searchRepository.searchItems(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = 10.0,
                    limit = 50
                ).onEach { result ->
                    _feedItems.value = result
                }.launchIn(viewModelScope)
            }
        }
    }
    
    fun loadNearbyStores() {
        locationManager.getLastKnownLocation { location ->
            if (location == null) {
                _nearbyStores.value = Resource.Error("Location not available. Please enable location services.")
                return@getLastKnownLocation
            }
            
            viewModelScope.launch {
                searchRepository.getNearbyStores(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    limit = 20
                ).onEach { result ->
                    _nearbyStores.value = result
                }.launchIn(viewModelScope)
            }
        }
    }
    
    fun refreshFeed() {
        loadFeedItems()
        loadNearbyStores()
    }
}

