package com.example.eco_plate.ui.home

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eco_plate.data.models.UserAddress
import com.example.eco_plate.ui.components.EcoColors
import com.example.eco_plate.utils.Resource
import com.google.android.gms.maps.CameraUpdateFactory
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
    viewModel: AddressViewModel = hiltViewModel()
) {
    val addresses by viewModel.addresses.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()
    val autocompleteResults by viewModel.locationAutofill.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val parsedAddress by viewModel.parsedAddress.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showMapConfirm by remember { mutableStateOf(false) }
    var showAddDetails by remember { mutableStateOf(false) }
    var showPlacePin by remember { mutableStateOf(false) }
    var pendingShowDetails by remember { mutableStateOf(false) } // For keyboard Enter action
    
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Map state
    val vancouver = LatLng(49.2827, -123.1207)
    val cameraPositionState = rememberCameraPositionState { 
        position = CameraPosition.fromLatLngZoom(vancouver, 14f) 
    }
    var markerPosition by remember { mutableStateOf(vancouver) }

    // Handle save state
    LaunchedEffect(saveState) {
        when (saveState) {
            is Resource.Success -> {
                Log.d("ChangeAddressScreen", "Address saved successfully")
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Address saved successfully!")
                }
                showAddDetails = false
                showMapConfirm = false
                showPlacePin = false
                searchQuery = ""
                viewModel.clearSaveState()
                viewModel.clearSelection()
            }
            is Resource.Error -> {
                Log.e("ChangeAddressScreen", "Save failed: ${(saveState as Resource.Error).message}")
                coroutineScope.launch {
                    snackbarHostState.showSnackbar((saveState as Resource.Error).message ?: "Failed to save address")
                }
                viewModel.clearSaveState()
            }
            else -> {}
        }
    }

    // When location selected from search
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            Log.d("ChangeAddressScreen", "Location selected: $it")
            try {
                markerPosition = it
                // Only show map confirm if we're not going directly to details
                if (!pendingShowDetails) {
                    showMapConfirm = true
                }
                // Animate camera after a small delay to ensure map is ready
                kotlinx.coroutines.delay(100)
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 16f))
            } catch (e: Exception) {
                Log.e("ChangeAddressScreen", "Error animating camera", e)
                markerPosition = it
                if (!pendingShowDetails) {
                    showMapConfirm = true
                }
            }
        }
    }
    
    // When pending show details and parsed address is ready
    LaunchedEffect(parsedAddress, pendingShowDetails) {
        if (pendingShowDetails && parsedAddress != null) {
            showAddDetails = true
            pendingShowDetails = false
        }
    }

    // Show Address Details Screen (full screen)
    if (showAddDetails && parsedAddress != null) {
        AddAddressDetailsScreen(
            parsedAddress = parsedAddress!!,
            onBack = {
                showAddDetails = false
                showMapConfirm = true
            },
            onSave = { label, unitNumber, buzzerCode, deliveryNotes, isDefault ->
                Log.d("ChangeAddressScreen", "Saving address: $label, unit: $unitNumber")
                // Update parsed address with unit info
                val updatedAddress = parsedAddress!!.copy(
                    line2 = listOfNotNull(
                        unitNumber?.let { "Unit $it" },
                        buzzerCode?.let { "Buzzer: $it" }
                    ).joinToString(", ").takeIf { it.isNotEmpty() }
                )
                viewModel.saveAddress(
                    label = label,
                    parsedAddress = updatedAddress,
                    isDefault = isDefault
                )
            },
            isSaving = saveState is Resource.Loading
        )
        return
    }

    // Show Place Pin Screen (full screen)
    if (showPlacePin) {
        PlacePinScreen(
            cameraPositionState = cameraPositionState,
            markerPosition = markerPosition,
            onBack = {
                showPlacePin = false
            },
            onMarkerMoved = { latLng ->
                markerPosition = latLng
            },
            onConfirm = { latLng ->
                // Get full address from pin coordinates
                viewModel.getAddressFromPin(latLng)
                showPlacePin = false
                showMapConfirm = true
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Address", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showMapConfirm) {
                            showMapConfirm = false
                            viewModel.clearSelection()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EcoColors.Green600,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        
        if (showMapConfirm) {
            // Map Confirmation Screen
            MapConfirmScreen(
                cameraPositionState = cameraPositionState,
                markerPosition = markerPosition,
                parsedAddress = parsedAddress,
                onMapClick = { latLng ->
                    markerPosition = latLng
                },
                onConfirm = {
                    if (parsedAddress != null) {
                        showAddDetails = true
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // Main Address Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        viewModel.searchPlaces(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search for an address") },
                    leadingIcon = { 
                        Icon(
                            Icons.Filled.Search, 
                            null,
                            tint = EcoColors.Green600
                        ) 
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { 
                                searchQuery = ""
                                viewModel.clearSearchResults()
                            }) {
                                Icon(Icons.Filled.Clear, "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoColors.Green600,
                        focusedLabelColor = EcoColors.Green600,
                        cursorColor = EcoColors.Green600
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            // Select first result if available and go directly to details
                            if (autocompleteResults.isNotEmpty()) {
                                val firstResult = autocompleteResults.first()
                                searchQuery = firstResult.address
                                pendingShowDetails = true // Will show details when address is parsed
                                viewModel.getCoordinates(firstResult)
                                viewModel.clearSearchResults()
                                focusManager.clearFocus()
                            }
                        }
                    )
                )

                // Show search results OR main content
                if (searchQuery.length >= 2 && autocompleteResults.isNotEmpty()) {
                    // Search Results
                    SearchResultsList(
                        results = autocompleteResults,
                        onResultSelect = { result ->
                            searchQuery = result.address
                            pendingShowDetails = true // Go directly to add details
                            viewModel.getCoordinates(result)
                            viewModel.clearSearchResults()
                            focusManager.clearFocus()
                        }
                    )
                } else {
                    // Main Content
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Quick Access Chips
                        item {
                            QuickAccessChips(
                                addresses = (addresses as? Resource.Success)?.data ?: emptyList(),
                                onChipClick = { address ->
                                    viewModel.selectAddress(address)
                                    onAddressConfirmed(address.fullAddress)
                                }
                            )
                        }
                        
                        // Place a pin option
                        item {
                            PlacePinOption(
                                onClick = {
                                    showPlacePin = true
                                }
                            )
                        }

                        // Saved Addresses Header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Saved Addresses",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EcoColors.Green800
                                )
                            }
                        }

                        // Saved Addresses List
                        when (addresses) {
                            is Resource.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = EcoColors.Green600)
                                    }
                                }
                            }
                            is Resource.Success -> {
                                val addressList = (addresses as Resource.Success).data ?: emptyList()
                                if (addressList.isEmpty()) {
                                    item {
                                        EmptyAddressHint()
                                    }
                                } else {
                                    items(addressList) { address ->
                                        SavedAddressItem(
                                            address = address,
                                            onClick = {
                                                viewModel.selectAddress(address)
                                                onAddressConfirmed(address.fullAddress)
                                            },
                                            onDelete = {
                                                viewModel.deleteAddress(address)
                                            },
                                            onSetDefault = {
                                                viewModel.setDefaultAddress(address)
                                            }
                                        )
                                    }
                                }
                            }
                            is Resource.Error -> {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Outlined.ErrorOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            "Failed to load addresses",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessChips(
    addresses: List<UserAddress>,
    onChipClick: (UserAddress) -> Unit
) {
    val homeAddress = addresses.find { it.label.equals("home", ignoreCase = true) }
    val workAddress = addresses.find { it.label.equals("work", ignoreCase = true) }
    val defaultAddress = addresses.find { it.isDefault }

    if (addresses.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Default/starred address
        defaultAddress?.let { address ->
            QuickChip(
                icon = Icons.Filled.Star,
                title = address.label,
                subtitle = address.shortAddress,
                isSelected = true,
                onClick = { onChipClick(address) }
            )
        }

        // Home chip
        homeAddress?.takeIf { it.id != defaultAddress?.id }?.let { address ->
            QuickChip(
                icon = Icons.Filled.Home,
                title = "Home",
                subtitle = address.shortAddress,
                isSelected = false,
                onClick = { onChipClick(address) }
            )
        }

        // Work chip
        workAddress?.takeIf { it.id != defaultAddress?.id }?.let { address ->
            QuickChip(
                icon = Icons.Filled.Work,
                title = "Work",
                subtitle = address.shortAddress,
                isSelected = false,
                onClick = { onChipClick(address) }
            )
        }
    }
}

@Composable
private fun QuickChip(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) EcoColors.Green100 else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) EcoColors.Green600 else Color.LightGray
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) EcoColors.Green600 else EcoColors.Green800
            )
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) EcoColors.Green800 else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 120.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<AutocompleteResult>,
    onResultSelect: (AutocompleteResult) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(results) { result ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResultSelect(result) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EcoColors.Green100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = EcoColors.Green600,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.primaryText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (result.secondaryText.isNotEmpty()) {
                        Text(
                            text = result.secondaryText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(start = 68.dp))
        }
    }
}

@Composable
private fun SavedAddressItem(
    address: UserAddress,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (address.isDefault) EcoColors.Green50 else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (address.isDefault) EcoColors.Green600 else EcoColors.Green100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (address.label.lowercase()) {
                        "home" -> Icons.Filled.Home
                        "work" -> Icons.Filled.Work
                        else -> Icons.Filled.LocationOn
                    },
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (address.isDefault) Color.White else EcoColors.Green600
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = address.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (address.isDefault) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = EcoColors.Green600
                        ) {
                            Text(
                                "Default",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = address.shortAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${address.city}, ${address.region} ${address.postalCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (!address.isDefault) {
                        DropdownMenuItem(
                            text = { Text("Set as default") },
                            leadingIcon = { Icon(Icons.Filled.Star, null, tint = EcoColors.Green600) },
                            onClick = {
                                showMenu = false
                                onSetDefault()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { 
                            Icon(
                                Icons.Filled.Delete, 
                                null, 
                                tint = MaterialTheme.colorScheme.error
                            ) 
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlacePinOption(
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EcoColors.Green50),
        border = androidx.compose.foundation.BorderStroke(1.dp, EcoColors.Green200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(EcoColors.Green600),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PinDrop,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Place a pin on map",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = EcoColors.Green800
                )
                Text(
                    "Drop a pin to select your exact location",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = EcoColors.Green600
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlacePinScreen(
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    markerPosition: LatLng,
    onBack: () -> Unit,
    onMarkerMoved: (LatLng) -> Unit,
    onConfirm: (LatLng) -> Unit
) {
    var currentMarker by remember { mutableStateOf(markerPosition) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Place a Pin", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EcoColors.Green600,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Instructions
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = EcoColors.Green50
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.TouchApp,
                        contentDescription = null,
                        tint = EcoColors.Green600
                    )
                    Text(
                        "Tap anywhere on the map to place your delivery pin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EcoColors.Green800
                    )
                }
            }
            
            // Map
            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        currentMarker = latLng
                        onMarkerMoved(latLng)
                    }
                ) {
                    Marker(
                        state = MarkerState(position = currentMarker),
                        title = "Delivery Location",
                        draggable = true
                    )
                }
            }
            
            // Confirm button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { onConfirm(currentMarker) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EcoColors.Green600)
                ) {
                    Icon(Icons.Filled.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Use This Location", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EmptyAddressHint() {
    Column(
                            modifier = Modifier
                                .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = EcoColors.Green500
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No saved addresses",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Search above to add your first delivery address",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun MapConfirmScreen(
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    markerPosition: LatLng,
    parsedAddress: ParsedAddress?,
    onMapClick: (LatLng) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Map
                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                onMapClick = onMapClick
                    ) {
                        Marker(
                            state = MarkerState(position = markerPosition),
                    title = "Delivery Location"
                )
            }
        }
        
        // Bottom card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                parsedAddress?.let { address ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EcoColors.Green100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = EcoColors.Green600,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = address.line1,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${address.city}, ${address.region} ${address.postalCode}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } ?: run {
                    Text(
                        "Loading address...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcoColors.Green600
                    ),
                    enabled = parsedAddress != null
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm Location", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAddressDetailsScreen(
    parsedAddress: ParsedAddress,
    onBack: () -> Unit,
    onSave: (label: String, unitNumber: String?, buzzerCode: String?, deliveryNotes: String?, isDefault: Boolean) -> Unit,
    isSaving: Boolean
) {
    val labelOptions = listOf("Home", "Work", "Other")
    var selectedLabel by remember { mutableStateOf("Home") }
    var customLabel by remember { mutableStateOf("") }
    var unitNumber by remember { mutableStateOf("") }
    var buzzerCode by remember { mutableStateOf("") }
    var deliveryNotes by remember { mutableStateOf("") }
    var setAsDefault by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Address Details", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EcoColors.Green600,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Address preview
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = EcoColors.Green50)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EcoColors.Green600),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            parsedAddress.line1,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${parsedAddress.city}, ${parsedAddress.region} ${parsedAddress.postalCode}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Apartment/Unit Details Section
            Text(
                "Apartment / Unit Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EcoColors.Green800
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = unitNumber,
                    onValueChange = { unitNumber = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Unit / Apt #") },
                    placeholder = { Text("e.g., 4B") },
                    leadingIcon = { 
                        Icon(
                            Icons.Outlined.Apartment, 
                            null,
                            tint = EcoColors.Green600
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoColors.Green600,
                        focusedLabelColor = EcoColors.Green600
                    ),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = buzzerCode,
                    onValueChange = { buzzerCode = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Buzzer Code") },
                    placeholder = { Text("e.g., #123") },
                    leadingIcon = { 
                        Icon(
                            Icons.Outlined.Dialpad, 
                            null,
                            tint = EcoColors.Green600
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoColors.Green600,
                        focusedLabelColor = EcoColors.Green600
                    ),
                    singleLine = true
                )
            }
            
            OutlinedTextField(
                value = deliveryNotes,
                onValueChange = { deliveryNotes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Delivery Instructions (Optional)") },
                placeholder = { Text("e.g., Leave at door, ring bell twice...") },
                leadingIcon = { 
                    Icon(
                        Icons.Outlined.Notes, 
                        null,
                        tint = EcoColors.Green600
                    ) 
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcoColors.Green600,
                    focusedLabelColor = EcoColors.Green600
                ),
                minLines = 2,
                maxLines = 3
            )
            
            HorizontalDivider()
            
            // Label selection
            Text(
                "Save Address As",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EcoColors.Green800
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                labelOptions.forEach { option ->
                    FilterChip(
                        selected = selectedLabel == option,
                        onClick = { selectedLabel = option },
                        label = { Text(option) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (option) {
                                    "Home" -> Icons.Filled.Home
                                    "Work" -> Icons.Filled.Work
                                    else -> Icons.Filled.LocationOn
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EcoColors.Green600,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
            
            // Custom label input
            AnimatedVisibility(visible = selectedLabel == "Other") {
                OutlinedTextField(
                    value = customLabel,
                    onValueChange = { customLabel = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Custom Label") },
                    placeholder = { Text("e.g., Mom's house, Gym") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoColors.Green600,
                        focusedLabelColor = EcoColors.Green600
                    ),
                    singleLine = true
                )
            }
            
            // Set as default
            Card(
                onClick = { setAsDefault = !setAsDefault },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (setAsDefault) EcoColors.Green50 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            null,
                            tint = if (setAsDefault) EcoColors.Green600 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column {
                            Text(
                                "Set as default address",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Use this address for future orders",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = setAsDefault,
                        onCheckedChange = { setAsDefault = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EcoColors.Green600
                        )
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Save button
            Button(
                onClick = {
                    val finalLabel = if (selectedLabel == "Other") customLabel.ifBlank { "Other" } else selectedLabel
                    onSave(
                        finalLabel,
                        unitNumber.takeIf { it.isNotBlank() },
                        buzzerCode.takeIf { it.isNotBlank() },
                        deliveryNotes.takeIf { it.isNotBlank() },
                        setAsDefault
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoColors.Green600),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Saving...", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                } else {
                    Icon(Icons.Filled.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Address", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
