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
                // Get postal code from Android's built-in Geocoder
                val postalCode = locationManager.getPostalCodeFromCoordinates(location.latitude, location.longitude)
                
                searchRepository.searchItems(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = 10.0,
                    limit = 50,
                    postalCode = postalCode
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
                // Get postal code from Android's built-in Geocoder
                val postalCode = locationManager.getPostalCodeFromCoordinates(location.latitude, location.longitude)
                
                // Use searchStores which has proper API wrapper format
                searchRepository.searchStores(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = 15.0,
                    limit = 20,
                    postalCode = postalCode
                ).onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            _nearbyStores.value = Resource.Success(result.data?.data ?: emptyList())
                        }
                        is Resource.Error -> {
                            _nearbyStores.value = Resource.Error(result.message ?: "Error loading stores")
                        }
                        is Resource.Loading -> {
                            _nearbyStores.value = Resource.Loading()
                        }
                    }
                }.launchIn(viewModelScope)
            }
        }
    }
    
    fun refreshFeed() {
        loadFeedItems()
        loadNearbyStores()
    }
}

