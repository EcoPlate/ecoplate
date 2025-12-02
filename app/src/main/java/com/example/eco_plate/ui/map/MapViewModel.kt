package com.example.eco_plate.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.Store
import com.example.eco_plate.data.repository.SearchRepository
import com.example.eco_plate.ui.location.LocationManager
import com.example.eco_plate.utils.Resource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

private const val TAG = "MapViewModel"

data class MapStore(
    val id: String,
    val name: String,
    val address: String,
    val location: LatLng,
    val type: String,
    val rating: Float = 4.5f,
    val distance: String = "",
    val distanceKm: Double = 0.0
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationManager: LocationManager,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _userLocation = MutableLiveData<LatLng?>()
    val userLocation: LiveData<LatLng?> = _userLocation

    private val _stores = MutableLiveData<Resource<List<MapStore>>>()
    val stores: LiveData<Resource<List<MapStore>>> = _stores

    private val _isLocationLoading = MutableLiveData(true)
    val isLocationLoading: LiveData<Boolean> = _isLocationLoading
    
    private val _locationError = MutableLiveData<String?>()
    val locationError: LiveData<String?> = _locationError
    
    // Track when user has moved the map away from their search area
    private val _showSearchThisArea = MutableLiveData(false)
    val showSearchThisArea: LiveData<Boolean> = _showSearchThisArea
    
    // Current search center and radius
    private var lastSearchCenter: LatLng? = null
    private var lastSearchRadius: Double = 25.0
    
    // All stores loaded (accumulate as user explores)
    private val allLoadedStores = mutableMapOf<String, MapStore>()
    
    private var cameraMovedJob: Job? = null

    init {
        // Start location updates first to improve chances of getting location
        locationManager.startLocationUpdates()
        loadUserLocation()
    }

    fun loadUserLocation() {
        _isLocationLoading.value = true
        _locationError.value = null
        
        Log.d(TAG, "Requesting user location...")
        
        locationManager.getLastKnownLocation { location ->
            if (location != null) {
                Log.d(TAG, "Got user location: ${location.latitude}, ${location.longitude}")
                _userLocation.postValue(LatLng(location.latitude, location.longitude))
                loadNearbyStores(location.latitude, location.longitude, 25.0)
            } else {
                Log.w(TAG, "Could not get user location, requesting fresh location...")
                // Request a fresh location if last known is null
                locationManager.requestFreshLocation { freshLocation ->
                    if (freshLocation != null) {
                        Log.d(TAG, "Got fresh location: ${freshLocation.latitude}, ${freshLocation.longitude}")
                        _userLocation.postValue(LatLng(freshLocation.latitude, freshLocation.longitude))
                        loadNearbyStores(freshLocation.latitude, freshLocation.longitude, 25.0)
                        _locationError.postValue(null)
                    } else {
                        Log.w(TAG, "Could not get user location, using default")
                        _locationError.postValue("Could not get your location. Please enable GPS and location permissions.")
                        // Default to Vancouver if no location - but show error
                        _userLocation.postValue(LatLng(49.2827, -123.1207))
                        loadNearbyStores(49.2827, -123.1207, 25.0)
                    }
                    _isLocationLoading.postValue(false)
                }
                return@getLastKnownLocation
            }
            _isLocationLoading.postValue(false)
        }
    }

    /**
     * Called when the camera moves - check if we need to show "Search this area" button
     */
    fun onCameraMoved(center: LatLng, visibleRadius: Double) {
        cameraMovedJob?.cancel()
        cameraMovedJob = viewModelScope.launch {
            delay(300) // Debounce
            
            lastSearchCenter?.let { searchCenter ->
                // Calculate distance between current center and last search center
                val distance = calculateDistance(
                    searchCenter.latitude, searchCenter.longitude,
                    center.latitude, center.longitude
                )
                
                // Show "Search this area" if user has moved significantly (more than 30% of last search radius)
                // or if the visible radius is much larger than the last search
                val shouldShowButton = distance > lastSearchRadius * 0.3 || 
                                       visibleRadius > lastSearchRadius * 1.5
                
                _showSearchThisArea.postValue(shouldShowButton)
            }
        }
    }
    
    /**
     * Calculate visible radius from map bounds
     */
    fun calculateVisibleRadius(bounds: LatLngBounds): Double {
        val center = bounds.center
        val northeast = bounds.northeast
        
        // Calculate distance from center to corner
        return calculateDistance(
            center.latitude, center.longitude,
            northeast.latitude, northeast.longitude
        )
    }
    
    /**
     * Search stores in the currently visible area
     */
    fun searchInVisibleArea(center: LatLng, radiusKm: Double) {
        _showSearchThisArea.value = false
        loadNearbyStores(center.latitude, center.longitude, radiusKm)
    }

    fun loadNearbyStores(latitude: Double, longitude: Double, radiusKm: Double = 25.0) {
        viewModelScope.launch {
            _stores.value = Resource.Loading()
            
            // Update last search position
            lastSearchCenter = LatLng(latitude, longitude)
            lastSearchRadius = radiusKm
            
            val postalCode = locationManager.getPostalCodeFromCoordinates(latitude, longitude)
            
            Log.d(TAG, "Loading stores at ($latitude, $longitude) with radius $radiusKm km")
            
            searchRepository.searchStores(
                latitude = latitude,
                longitude = longitude,
                radius = radiusKm,
                limit = 100, // Load more stores for map view
                postalCode = postalCode
            ).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        val newStores = result.data?.data?.map { store ->
                            MapStore(
                                id = store.id,
                                name = store.name,
                                address = store.address ?: store.city ?: "Unknown",
                                location = LatLng(
                                    store.latitude ?: latitude,
                                    store.longitude ?: longitude
                                ),
                                type = store.type ?: "Grocery",
                                rating = store.rating?.toFloat() ?: 4.5f,
                                distance = store.distanceFormatted ?: formatDistance(store.distanceKm),
                                distanceKm = store.distanceKm ?: 0.0
                            )
                        } ?: emptyList()
                        
                        // Add new stores to our accumulated map
                        newStores.forEach { store ->
                            allLoadedStores[store.id] = store
                        }
                        
                        Log.d(TAG, "Loaded ${newStores.size} new stores, total accumulated: ${allLoadedStores.size}")
                        
                        // Return all accumulated stores
                        _stores.value = Resource.Success(allLoadedStores.values.toList())
                        _showSearchThisArea.value = false
                    }
                    is Resource.Error -> {
                        // On error, still show previously loaded stores if we have them
                        if (allLoadedStores.isNotEmpty()) {
                            _stores.value = Resource.Success(allLoadedStores.values.toList())
                        } else {
                            _stores.value = Resource.Error(result.message ?: "Failed to load stores")
                        }
                    }
                    is Resource.Loading -> {
                        // Keep showing existing stores while loading new ones
                        if (allLoadedStores.isNotEmpty()) {
                            _stores.value = Resource.Success(allLoadedStores.values.toList())
                        } else {
                            _stores.value = Resource.Loading()
                        }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) + 
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * 
                sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun formatDistance(distanceKm: Double?): String {
        return when {
            distanceKm == null -> ""
            distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()} m"
            else -> String.format("%.1f km", distanceKm)
        }
    }

    fun refreshStores() {
        _userLocation.value?.let { location ->
            loadNearbyStores(location.latitude, location.longitude, lastSearchRadius)
        }
    }
    
    /**
     * Clear all loaded stores and reload
     */
    fun clearAndReload() {
        allLoadedStores.clear()
        refreshStores()
    }
}
