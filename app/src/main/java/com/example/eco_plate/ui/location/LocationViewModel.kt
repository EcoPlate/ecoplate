package com.example.eco_plate.ui.location

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.repository.UserRepository
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationManager: LocationManager,
    private val userRepository: UserRepository
) : ViewModel() {
    
    val currentLocation: LiveData<Location?> = locationManager.currentLocation
    val locationPermissionGranted: LiveData<Boolean> = locationManager.locationPermissionGranted
    val currentPostalCode: LiveData<String?> = locationManager.currentPostalCode
    
    private val _locationUpdateState = MutableLiveData<Resource<com.example.eco_plate.data.models.User>>()
    val locationUpdateState: LiveData<Resource<com.example.eco_plate.data.models.User>> = _locationUpdateState
    
    init {
        // Start location updates if permission is already granted
        if (locationManager.checkLocationPermission()) {
            locationManager.startLocationUpdates()
        }
        
        // Observe location changes and update backend
        currentLocation.observeForever { location ->
            location?.let {
                updateLocationInBackend(it.latitude, it.longitude)
            }
        }
    }
    
    fun requestLocationPermission() {
        // This will be called from the Activity/Fragment
        // The actual permission request should be handled in the UI layer
        locationManager.checkLocationPermission()
    }
    
    fun onPermissionGranted() {
        locationManager.startLocationUpdates()
    }
    
    fun onPermissionDenied() {
        // Handle permission denied case
        _locationUpdateState.value = Resource.Error("Location permission is required for this feature")
    }
    
    private fun updateLocationInBackend(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            // Get postal code using Android's built-in Geocoder (no external API needed!)
            val postalCode = locationManager.getPostalCodeFromCoordinates(latitude, longitude)
            
            userRepository.updateLocation(latitude, longitude, postalCode)
                .onEach { result ->
                    _locationUpdateState.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    /**
     * Get the current cached postal code from the last location update
     */
    fun getCachedPostalCode(): String? {
        return locationManager.getCachedPostalCode()
    }
    
    fun stopLocationUpdates() {
        locationManager.stopLocationUpdates()
    }
    
    override fun onCleared() {
        super.onCleared()
        locationManager.stopLocationUpdates()
    }
}
