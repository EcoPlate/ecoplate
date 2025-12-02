package com.example.eco_plate.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    
    // Cached postal code from last geocoding
    private val _currentPostalCode = MutableLiveData<String?>()
    val currentPostalCode: LiveData<String?> = _currentPostalCode
    
    private val geocoder: Geocoder = Geocoder(context, Locale.CANADA)
    
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
                if (location != null) {
                    _currentLocation.value = location
                callback(location)
                } else {
                    // Last known location is null, request a fresh location
                    Log.d(TAG, "Last known location is null, requesting fresh location...")
                    requestFreshLocation(callback)
                }
            }.addOnFailureListener {
                Log.e(TAG, "Failed to get last known location: ${it.message}")
                requestFreshLocation(callback)
            }
        } else {
            Log.w(TAG, "Location permission not granted")
            callback(null)
        }
    }
    
    @SuppressLint("MissingPermission")
    fun requestFreshLocation(callback: (Location?) -> Unit) {
        if (!checkLocationPermission()) {
            callback(null)
            return
        }
        
        val freshLocationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L // 1 second
        ).apply {
            setMaxUpdates(1) // Only get one update
            setMaxUpdateDelayMillis(5000L) // Max 5 seconds to get location
        }.build()
        
        val freshLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                val location = locationResult.lastLocation
                if (location != null) {
                    Log.d(TAG, "Got fresh location: ${location.latitude}, ${location.longitude}")
                    _currentLocation.value = location
                    callback(location)
                } else {
                    Log.w(TAG, "Fresh location request returned null")
                    callback(null)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            freshLocationRequest,
            freshLocationCallback,
            Looper.getMainLooper()
        )
    }
    
    /**
     * Get postal code from coordinates using Android's built-in Geocoder.
     * This uses Google's geocoding service through Play Services - no API key required!
     */
    @Suppress("DEPRECATION")
    suspend fun getPostalCodeFromCoordinates(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) {
                    Log.w(TAG, "Geocoder not available on this device")
                    return@withContext null
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Use the new async API for Android 13+
                    var result: String? = null
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            result = addresses[0].postalCode
                            _currentPostalCode.postValue(result)
                        }
                    }
                    // Give async callback time to complete
                    kotlinx.coroutines.delay(500)
                    result
                } else {
                    // Use the deprecated synchronous API for older Android versions
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val postalCode = addresses[0].postalCode
                        _currentPostalCode.postValue(postalCode)
                        Log.d(TAG, "Got postal code: $postalCode from coordinates ($latitude, $longitude)")
                        postalCode
                    } else {
                        Log.w(TAG, "No address found for coordinates ($latitude, $longitude)")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reverse geocode: ${e.message}")
                null
            }
        }
    }
    
    /**
     * Get the last cached postal code
     */
    fun getCachedPostalCode(): String? {
        return _currentPostalCode.value
    }
    
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "LocationManager"
    }
}
