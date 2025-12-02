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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

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
    
    // Delivery address location (to show pin on map)
    private val _deliveryAddressLocation = MutableLiveData<LatLng?>(null)
    val deliveryAddressLocation: LiveData<LatLng?> = _deliveryAddressLocation
    
    private val _deliveryAddressLabel = MutableLiveData<String?>(null)
    val deliveryAddressLabel: LiveData<String?> = _deliveryAddressLabel
    
    // Current search center and radius
    private var lastSearchCenter: LatLng? = null
    private var lastSearchRadius: Double = 10.0
    
    // All stores loaded (accumulate as user explores)
    private val allLoadedStores = mutableMapOf<String, MapStore>()
    
    private var cameraMovedJob: Job? = null

    init {
        // Don't auto-load - wait for delivery address or explicit call
        locationManager.startLocationUpdates()
    }

    /**
     * Load the map centered on delivery address if available, otherwise GPS
     */
    fun loadForDeliveryAddress(latitude: Double?, longitude: Double?) {
        _isLocationLoading.value = true
        
        if (latitude != null && longitude != null) {
            Log.d(TAG, "Loading map for delivery address: $latitude, $longitude")
            val location = LatLng(latitude, longitude)
            _userLocation.postValue(location)
            _deliveryAddressLocation.postValue(location)
            loadNearbyStores(latitude, longitude, 10.0)
            _isLocationLoading.postValue(false)
        } else {
            Log.d(TAG, "No delivery address, falling back to GPS")
            loadUserLocation()
        }
    }

    fun loadUserLocation() {
        _isLocationLoading.value = true
        _locationError.value = null
        
        Log.d(TAG, "Requesting user GPS location...")
        
        locationManager.getLastKnownLocation { location ->
            if (location != null) {
                Log.d(TAG, "Got GPS location: ${location.latitude}, ${location.longitude}")
                _userLocation.postValue(LatLng(location.latitude, location.longitude))
                loadNearbyStores(location.latitude, location.longitude, 10.0)
            } else {
                Log.w(TAG, "Could not get GPS location, requesting fresh...")
                locationManager.requestFreshLocation { freshLocation ->
                    if (freshLocation != null) {
                        Log.d(TAG, "Got fresh GPS: ${freshLocation.latitude}, ${freshLocation.longitude}")
                        _userLocation.postValue(LatLng(freshLocation.latitude, freshLocation.longitude))
                        loadNearbyStores(freshLocation.latitude, freshLocation.longitude, 10.0)
                        _locationError.postValue(null)
                    } else {
                        Log.w(TAG, "No GPS available, using Vancouver default")
                        _locationError.postValue("Could not get your location.")
                        _userLocation.postValue(LatLng(49.2827, -123.1207))
                        loadNearbyStores(49.2827, -123.1207, 10.0)
                    }
                    _isLocationLoading.postValue(false)
                }
                return@getLastKnownLocation
            }
            _isLocationLoading.postValue(false)
        }
    }

    /**
     * Called when the camera moves - automatically load stores for new area
     */
    fun onCameraMoved(center: LatLng, visibleRadius: Double) {
        cameraMovedJob?.cancel()
        cameraMovedJob = viewModelScope.launch {
            delay(1500) // Longer debounce to prevent 429 rate limiting
            
            val searchCenter = lastSearchCenter
            
            if (searchCenter == null) {
                // First load - search current area
                Log.d(TAG, "First camera move, loading stores at center")
                loadNearbyStores(center.latitude, center.longitude, maxOf(visibleRadius, 10.0))
                return@launch
            }
            
            // Calculate distance between current center and last search center
            val distance = calculateDistance(
                searchCenter.latitude, searchCenter.longitude,
                center.latitude, center.longitude
            )
            
            // Automatically load new stores if:
            // 1. User moved more than 20% of the visible radius away
            // 2. OR the visible radius is significantly different (zoomed in/out)
            val shouldAutoSearch = distance > visibleRadius * 0.2 || 
                                  abs(visibleRadius - lastSearchRadius) > lastSearchRadius * 0.3
            
            if (shouldAutoSearch) {
                Log.d(TAG, "Auto-searching new area: distance=$distance, visibleRadius=$visibleRadius")
                loadNearbyStores(center.latitude, center.longitude, maxOf(visibleRadius * 1.2, 10.0))
            } else {
                // Show button for manual search if moved slightly
                val shouldShowButton = distance > lastSearchRadius * 0.15
                _showSearchThisArea.postValue(shouldShowButton)
            }
        }
    }
    
    /**
     * Update search location based on selected delivery address
     */
    fun updateDeliveryLocation(latitude: Double, longitude: Double, label: String? = null) {
        Log.d(TAG, "Updating to delivery address location: $latitude, $longitude ($label)")
        val location = LatLng(latitude, longitude)
        _userLocation.postValue(location)
        _deliveryAddressLocation.postValue(location)
        _deliveryAddressLabel.postValue(label ?: "Delivery Address")
        allLoadedStores.clear() // Clear old stores
        loadNearbyStores(latitude, longitude, 15.0) // Use larger radius for map
    }
    
    /**
     * Set the delivery address pin location
     */
    fun setDeliveryAddressPin(latitude: Double, longitude: Double, label: String = "Delivery Address") {
        _deliveryAddressLocation.postValue(LatLng(latitude, longitude))
        _deliveryAddressLabel.postValue(label)
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

    fun loadNearbyStores(latitude: Double, longitude: Double, radiusKm: Double = 10.0) {
        viewModelScope.launch {
            // CLEAR old stores when searching a new location
            allLoadedStores.clear()
            _stores.value = Resource.Loading()
            
            // Update last search position
            lastSearchCenter = LatLng(latitude, longitude)
            lastSearchRadius = radiusKm
            
            val postalCode = locationManager.getPostalCodeFromCoordinates(latitude, longitude)
            
            Log.d(TAG, "Loading stores at ($latitude, $longitude) with radius $radiusKm km - CLEARED OLD STORES")
            
            searchRepository.searchStores(
                latitude = latitude,
                longitude = longitude,
                radius = radiusKm,
                limit = 500, // No practical limit - load all stores in area
                postalCode = postalCode
            ).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        val searchCenter = LatLng(latitude, longitude)
                        val newStores = result.data?.data?.mapNotNull { store ->
                            val storeLat = store.latitude ?: return@mapNotNull null
                            val storeLng = store.longitude ?: return@mapNotNull null
                            
                            // Skip stores at exactly the same location as search center (likely wrong data)
                            val latDiff = kotlin.math.abs(storeLat - latitude)
                            val lngDiff = kotlin.math.abs(storeLng - longitude)
                            if (latDiff < 0.0001 && lngDiff < 0.0001) {
                                Log.w(TAG, "Skipping store '${store.name}' - at same location as delivery address")
                                return@mapNotNull null
                            }
                            
                            MapStore(
                                id = store.id,
                                name = store.name,
                                address = store.address ?: store.city ?: "Unknown",
                                location = LatLng(storeLat, storeLng),
                                type = store.type ?: "Grocery",
                                rating = store.rating?.toFloat() ?: 4.5f,
                                distance = store.distanceFormatted ?: formatDistance(store.distanceKm),
                                distanceKm = store.distanceKm ?: 0.0
                            )
                        } ?: emptyList()
                        
                        // Only show stores returned by API (already filtered by radius)
                        allLoadedStores.clear()
                        newStores.forEach { store ->
                            allLoadedStores[store.id] = store
                        }
                        
                        Log.d(TAG, "Showing ${newStores.size} stores within ${radiusKm}km radius")
                        
                        _stores.value = Resource.Success(newStores)
                        _showSearchThisArea.value = false
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Error loading stores: ${result.message}")
                        _stores.value = Resource.Error(result.message ?: "Failed to load stores")
                    }
                    is Resource.Loading -> {
                        _stores.value = Resource.Loading()
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
