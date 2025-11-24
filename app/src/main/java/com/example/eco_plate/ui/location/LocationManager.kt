package com.example.eco_plate.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?> = _currentLocation
    
    private val _locationPermissionGranted = MutableLiveData<Boolean>()
    val locationPermissionGranted: LiveData<Boolean> = _locationPermissionGranted
    
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                _currentLocation.value = location
            }
        }
    }
    
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        10000L // 10 seconds
    ).apply {
        setMinUpdateIntervalMillis(5000L) // 5 seconds
        setMaxUpdateDelayMillis(15000L) // 15 seconds
    }.build()
    
    fun checkLocationPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        _locationPermissionGranted.value = hasPermission
        return hasPermission
    }
    
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            
            // Get last known location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    _currentLocation.value = it
                }
            }
        }
    }
    
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(callback: (Location?) -> Unit) {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                callback(location)
            }.addOnFailureListener {
                callback(null)
            }
        } else {
            callback(null)
        }
    }
    
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
