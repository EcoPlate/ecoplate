package com.example.eco_plate.ui.home

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.CreateAddressRequest
import com.example.eco_plate.data.models.UserAddress
import com.example.eco_plate.data.repository.AddressRepository
import com.example.eco_plate.utils.Resource
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class AutocompleteResult(
    val address: String,
    val placeId: String,
    val primaryText: String = "",
    val secondaryText: String = ""
)

data class ParsedAddress(
    val line1: String,
    val line2: String? = null,
    val city: String,
    val region: String,
    val postalCode: String,
    val country: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val placeId: String? = null
)

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    private val placesClient: PlacesClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "AddressViewModel"

    // Saved addresses
    private val _addresses = MutableStateFlow<Resource<List<UserAddress>>>(Resource.Loading())
    val addresses: StateFlow<Resource<List<UserAddress>>> = _addresses.asStateFlow()

    // Selected/default address
    private val _selectedAddress = MutableStateFlow<UserAddress?>(null)
    val selectedAddress: StateFlow<UserAddress?> = _selectedAddress.asStateFlow()

    // Places autocomplete
    private val _locationAutofill = MutableStateFlow<List<AutocompleteResult>>(emptyList())
    val locationAutofill: StateFlow<List<AutocompleteResult>> = _locationAutofill.asStateFlow()

    // Selected location from search
    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation.asStateFlow()

    // Parsed address from selection
    private val _parsedAddress = MutableStateFlow<ParsedAddress?>(null)
    val parsedAddress: StateFlow<ParsedAddress?> = _parsedAddress.asStateFlow()

    // Operation states
    private val _saveState = MutableStateFlow<Resource<UserAddress>?>(null)
    val saveState: StateFlow<Resource<UserAddress>?> = _saveState.asStateFlow()

    private val _deleteState = MutableStateFlow<Resource<Boolean>?>(null)
    val deleteState: StateFlow<Resource<Boolean>?> = _deleteState.asStateFlow()

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            addressRepository.getAddresses()
                .onEach { result ->
                    _addresses.value = result
                    if (result is Resource.Success) {
                        // Set selected address to default or first
                        val defaultAddress = result.data?.find { it.isDefault }
                            ?: result.data?.firstOrNull()
                        _selectedAddress.value = defaultAddress
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun selectAddress(address: UserAddress) {
        _selectedAddress.value = address
    }

    fun setDefaultAddress(address: UserAddress) {
        viewModelScope.launch {
            addressRepository.setDefaultAddress(address.id)
                .onEach { result ->
                    if (result is Resource.Success) {
                        loadAddresses()
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun deleteAddress(address: UserAddress) {
        viewModelScope.launch {
            addressRepository.deleteAddress(address.id)
                .onEach { result ->
                    _deleteState.value = result
                    if (result is Resource.Success) {
                        loadAddresses()
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun saveAddress(
        label: String,
        parsedAddress: ParsedAddress,
        isDefault: Boolean = false
    ) {
        viewModelScope.launch {
            val request = CreateAddressRequest(
                label = label,
                line1 = parsedAddress.line1,
                line2 = parsedAddress.line2,
                city = parsedAddress.city,
                region = parsedAddress.region,
                postalCode = parsedAddress.postalCode,
                country = parsedAddress.country,
                latitude = parsedAddress.latitude,
                longitude = parsedAddress.longitude,
                placeId = parsedAddress.placeId,
                isDefault = isDefault
            )
            
            addressRepository.createAddress(request)
                .onEach { result ->
                    _saveState.value = result
                    if (result is Resource.Success) {
                        loadAddresses()
                        result.data?.let { _selectedAddress.value = it }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun clearSaveState() {
        _saveState.value = null
    }

    fun clearDeleteState() {
        _deleteState.value = null
    }

    // Places Search
    fun searchPlaces(query: String) {
        if (query.length < 3) {
            _locationAutofill.value = emptyList()
            return
        }

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("CA") // Restrict to Canada
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                _locationAutofill.value = response.autocompletePredictions.map {
                    AutocompleteResult(
                        address = it.getFullText(null).toString(),
                        placeId = it.placeId,
                        primaryText = it.getPrimaryText(null).toString(),
                        secondaryText = it.getSecondaryText(null).toString()
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Places search failed", e)
                _locationAutofill.value = emptyList()
            }
    }

    fun clearSearchResults() {
        _locationAutofill.value = emptyList()
    }

    fun getCoordinates(result: AutocompleteResult) {
        Log.d(TAG, "Getting coordinates for: ${result.address}, placeId: ${result.placeId}")
        
        // If placeId is empty, we can't fetch from Places API - use geocoder instead
        if (result.placeId.isBlank()) {
            Log.w(TAG, "No placeId, falling back to geocoder")
            viewModelScope.launch {
                try {
                    val geocoder = Geocoder(context)
                    val addresses = geocoder.getFromLocationName(result.address, 1)
                    val address = addresses?.firstOrNull()
                    if (address != null) {
                        val latLng = LatLng(address.latitude, address.longitude)
                        _selectedLocation.value = latLng
                        _parsedAddress.value = ParsedAddress(
                            line1 = result.primaryText.ifEmpty { address.getAddressLine(0)?.split(",")?.firstOrNull() ?: "Unknown" },
                            city = address.locality ?: address.subAdminArea ?: "Unknown City",
                            region = address.adminArea ?: "BC",
                            postalCode = address.postalCode ?: "",
                            country = address.countryName ?: "Canada",
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            placeId = null
                        )
                        Log.d(TAG, "Geocoded address: ${_parsedAddress.value}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Geocoding failed", e)
                }
            }
            return
        }
        
        try {
            val placeFields = listOf(
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.ADDRESS
            )
            val request = FetchPlaceRequest.newInstance(result.placeId, placeFields)

            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val place = response.place
                    Log.d(TAG, "Place fetched: ${place.address}")
                    place.latLng?.let { latLng ->
                        _selectedLocation.value = latLng
                        
                        // Parse address components
                        val components = place.addressComponents?.asList() ?: emptyList()
                        
                        var streetNumber = ""
                        var streetName = ""
                        var city = ""
                        var region = ""
                        var postalCode = ""
                        var country = "Canada"
                        
                        for (component in components) {
                            val types = component.types
                            when {
                                types.contains("street_number") -> streetNumber = component.name
                                types.contains("route") -> streetName = component.name
                                types.contains("locality") -> city = component.name
                                types.contains("administrative_area_level_1") -> region = component.shortName ?: component.name
                                types.contains("postal_code") -> postalCode = component.name
                                types.contains("country") -> country = component.name
                            }
                        }
                        
                        val line1 = if (streetNumber.isNotEmpty() && streetName.isNotEmpty()) {
                            "$streetNumber $streetName"
                        } else {
                            result.primaryText.ifEmpty { place.address ?: "Unknown Address" }
                        }
                        
                        _parsedAddress.value = ParsedAddress(
                            line1 = line1,
                            city = city.ifEmpty { "Unknown City" },
                            region = region.ifEmpty { "BC" },
                            postalCode = postalCode.ifEmpty { "" },
                            country = country,
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            placeId = result.placeId
                        )
                        Log.d(TAG, "Parsed address: ${_parsedAddress.value}")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to fetch place details", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getCoordinates", e)
        }
    }

    fun getAddressFromCoordinates(latLng: LatLng): String {
        val geocoder = Geocoder(context)
        return try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Address"
        } catch (e: IOException) {
            "Error finding address"
        }
    }
    
    /**
     * Get full parsed address from map pin coordinates using Geocoder
     */
    fun getAddressFromPin(latLng: LatLng) {
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context)
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                val address = addresses?.firstOrNull()
                
                if (address != null) {
                    val streetNumber = address.subThoroughfare ?: ""
                    val streetName = address.thoroughfare ?: ""
                    val line1 = if (streetNumber.isNotEmpty() && streetName.isNotEmpty()) {
                        "$streetNumber $streetName"
                    } else {
                        address.getAddressLine(0)?.split(",")?.firstOrNull() ?: "Unknown Address"
                    }
                    
                    _parsedAddress.value = ParsedAddress(
                        line1 = line1,
                        city = address.locality ?: address.subAdminArea ?: "Unknown City",
                        region = address.adminArea ?: "BC",
                        postalCode = address.postalCode ?: "",
                        country = address.countryName ?: "Canada",
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        placeId = null
                    )
                    _selectedLocation.value = latLng
                    Log.d(TAG, "Parsed address from pin: ${_parsedAddress.value}")
                } else {
                    Log.e(TAG, "No address found for coordinates: $latLng")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to geocode pin location", e)
            }
        }
    }

    fun clearSelection() {
        _selectedLocation.value = null
        _parsedAddress.value = null
    }
}

