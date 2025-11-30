package com.example.eco_plate.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eco_plate.ui.components.ModernSearchBar
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeAddressScreen(
    onNavigateBack: () -> Unit,
    onAddressConfirmed: (String) -> Unit,
    viewModel: ChangeAddressViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val autocompleteResults by viewModel.locationAutofill.collectAsState()
    val newCoordinates by viewModel.selectedLocation.collectAsState()

    // Map state
    val vancouver = LatLng(49.2827, -123.1207)
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(vancouver, 12f) }
    var markerPosition by remember { mutableStateOf(vancouver) }

    // When the ViewModel provides new coordinates, move the map and marker
    LaunchedEffect(newCoordinates) {
        newCoordinates?.let {
            cameraPositionState.animate(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 15f))
            markerPosition = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Address") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom=100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search Bar at the top
            ModernSearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.searchPlaces(it) // Search as user types
                },
                placeholder = "Search for an address",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Show predictions or the map
            if (autocompleteResults.isNotEmpty() && searchQuery.isNotEmpty()) {
                // 1. Show Autocomplete Results
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(autocompleteResults) { result ->
                        Text(
                            text = result.address,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchQuery = result.address // Set text to the selected address
                                    viewModel.getCoordinates(result) // Ask ViewModel for LatLng
                                    viewModel.searchPlaces("") // Clear the search results
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            } else {
                // 2. Show the Map
                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { markerPosition = it } // Allow user to tap to select
                    ) {
                        Marker(
                            state = MarkerState(position = markerPosition),
                            title = "Selected Address"
                        )
                    }
                }

                Button(
                    onClick = {
                        val finalAddress = viewModel.getAddressFromCoordinates(markerPosition)
                        onAddressConfirmed(finalAddress)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp)
                ) {
                    Text("Confirm Address")
                }
            }
        }
    }
}
