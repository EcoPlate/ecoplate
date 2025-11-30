package com.example.eco_plate.ui.home

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class AutocompleteResult(
    val address: String,
    val placeId: String
)

@HiltViewModel
class ChangeAddressViewModel @Inject constructor(
    private val placesClient: PlacesClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _locationAutofill = MutableStateFlow<List<AutocompleteResult>>(emptyList())
    val locationAutofill = _locationAutofill.asStateFlow()

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()

    fun searchPlaces(query: String) {
        if (query.isEmpty()) {
            _locationAutofill.value = emptyList()
            return
        }

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                _locationAutofill.value = response.autocompletePredictions.map {
                    AutocompleteResult(it.getFullText(null).toString(), it.placeId)
                }
            }
    }

    fun getCoordinates(result: AutocompleteResult) {
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(result.placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                response.place.latLng?.let {
                    _selectedLocation.value = it
                }
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
}
