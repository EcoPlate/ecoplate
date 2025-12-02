package com.example.eco_plate.ui.map

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eco_plate.ui.components.EcoColors
import com.example.eco_plate.utils.Resource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SafeGoogleMapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    deliveryLatitude: Double? = null,
    deliveryLongitude: Double? = null,
    onBackClick: () -> Unit = {},
    onStoreClick: (String) -> Unit = {},
    onCallDriver: () -> Unit = {},
    onMessageDriver: () -> Unit = {},
    onReportIssue: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedStore by remember { mutableStateOf<MapStore?>(null) }
    var isBottomSheetExpanded by remember { mutableStateOf(false) }
    var mapLoadError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Observe ViewModel data
    val userLocation by viewModel.userLocation.observeAsState()
    val storesResource by viewModel.stores.observeAsState()
    val isLocationLoading by viewModel.isLocationLoading.observeAsState(true)
    val locationError by viewModel.locationError.observeAsState()
    val deliveryAddressLocation by viewModel.deliveryAddressLocation.observeAsState()
    val deliveryAddressLabel by viewModel.deliveryAddressLabel.observeAsState()
    
    // Permission state
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    // Load map for delivery address or GPS on launch
    LaunchedEffect(Unit) {
        viewModel.loadForDeliveryAddress(deliveryLatitude, deliveryLongitude)
    }
    
    // Request permission if not granted
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }
    
    // Show error snackbar if location failed
    LaunchedEffect(locationError) {
        locationError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Retry",
                duration = SnackbarDuration.Long
            ).let { result ->
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.loadUserLocation()
                }
            }
        }
    }
    
    // Get stores from API
    val stores = when (val resource = storesResource) {
        is Resource.Success -> resource.data ?: emptyList()
        else -> emptyList()
    }
    
    val isLoading = storesResource is Resource.Loading || isLocationLoading
    
    // Default location (Downtown Vancouver) if user location not available
    val currentLocation = userLocation ?: LatLng(49.2827, -123.1207)
    
    // Camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 13f)
    }
    
    // Track if we should show "Search this area" button
    val showSearchThisArea by viewModel.showSearchThisArea.observeAsState(false)
    
    // Update camera when user location changes (only on first load)
    var hasInitializedCamera by remember { mutableStateOf(false) }
    LaunchedEffect(userLocation) {
        if (!hasInitializedCamera && userLocation != null) {
            userLocation?.let { location ->
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(location, 14f)
                )
                hasInitializedCamera = true
            }
        }
    }
    
    // Listen for camera movements to detect when user explores new areas
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving && hasInitializedCamera) {
            // Camera stopped moving, check if we should show "Search this area"
            val projection = cameraPositionState.projection
            val bounds = projection?.visibleRegion?.latLngBounds
            if (bounds != null) {
                val center = cameraPositionState.position.target
                val visibleRadius = viewModel.calculateVisibleRadius(bounds)
                viewModel.onCameraMoved(center, visibleRadius)
            }
        }
    }
    
    // Map properties
    val mapProperties by remember(locationPermissionState.status.isGranted) {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = locationPermissionState.status.isGranted,
                mapType = MapType.NORMAL
            )
        )
    }
    
    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = true
            )
        )
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Try to load Google Map with error handling
        if (!mapLoadError) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings,
                onMapLoaded = {
                    // Map loaded successfully
                    coroutineScope.launch {
                        delay(500)
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(currentLocation, 14f)
                        )
                    }
                }
            ) {
                // Current location marker (GPS)
                Marker(
                    state = MarkerState(position = currentLocation),
                    title = "Your Location",
                    snippet = "You are here",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
                
                // Delivery address marker (if different from current location)
                deliveryAddressLocation?.let { deliveryLocation ->
                    // Only show if it's different from current GPS location
                    val latDiff = deliveryLocation.latitude - currentLocation.latitude
                    val lngDiff = deliveryLocation.longitude - currentLocation.longitude
                    val distance = kotlin.math.sqrt(latDiff * latDiff + lngDiff * lngDiff)
                    if (distance > 0.001) { // More than ~100m apart
                        Marker(
                            state = MarkerState(position = deliveryLocation),
                            title = deliveryAddressLabel ?: "Delivery Address",
                            snippet = "Your delivery location",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)
                        )
                    }
                }
                
                // Store markers from API
                stores.forEach { store ->
                    Marker(
                        state = MarkerState(position = store.location),
                        title = store.name,
                        snippet = "${store.type} • ${store.rating}★ • ${store.distance}",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            when (store.type.uppercase()) {
                                "SUPERMARKET", "GROCERY" -> BitmapDescriptorFactory.HUE_GREEN
                                "RESTAURANT" -> BitmapDescriptorFactory.HUE_ORANGE
                                "BAKERY" -> BitmapDescriptorFactory.HUE_YELLOW
                                "CAFE" -> BitmapDescriptorFactory.HUE_CYAN
                                "DELI" -> BitmapDescriptorFactory.HUE_VIOLET
                                else -> BitmapDescriptorFactory.HUE_RED
                            }
                        ),
                        onClick = {
                            selectedStore = store
                            isBottomSheetExpanded = true
                            false
                        }
                    )
                }
                
                // Draw route from selected store to current location
                selectedStore?.let { store ->
                    Polyline(
                        points = listOf(
                            store.location,
                            LatLng(
                                (store.location.latitude + currentLocation.latitude) / 2,
                                (store.location.longitude + currentLocation.longitude) / 2
                            ),
                            currentLocation
                        ),
                        color = Color(0xFF2BAE66),
                        width = 10f
                    )
                }
            }
        }
        
        // Fallback UI if map fails to load
        if (mapLoadError) {
            FallbackMapView(
                stores = stores,
                isLoading = isLoading,
                onStoreClick = { store ->
                    selectedStore = store
                    isBottomSheetExpanded = true
                },
                onRefresh = { viewModel.refreshStores() }
            )
        }
        
        // Loading indicator
        if (isLoading && !mapLoadError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Finding nearby stores...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // Gradient overlay at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopCenter)
        )
        
        // Top Bar
        MapTopBar(
            storeCount = stores.size,
            onBackClick = onBackClick,
            onRefresh = { viewModel.clearAndReload() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        )
        
        // Map control buttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // My location button
            FloatingActionButton(
                onClick = {
                    if (!locationPermissionState.status.isGranted) {
                        locationPermissionState.launchPermissionRequest()
                    } else {
                        // Reload user location and search for stores there
                        viewModel.loadUserLocation()
                        coroutineScope.launch {
                            delay(500) // Give time for location to update
                            userLocation?.let { location ->
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(location, 14f)
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "My Location",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Zoom controls
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Zoom In",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Zoom Out",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // "Search this area" button - shown when user pans/zooms the map
        AnimatedVisibility(
            visible = showSearchThisArea && !isLoading,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Button(
                onClick = {
                    val projection = cameraPositionState.projection
                    val bounds = projection?.visibleRegion?.latLngBounds
                    if (bounds != null) {
                        val center = cameraPositionState.position.target
                        val visibleRadius = viewModel.calculateVisibleRadius(bounds)
                        viewModel.searchInVisibleArea(center, visibleRadius)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Search this area",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Bottom Sheet with Store Info
        // Snackbar for errors
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 220.dp)
        )
        
        // Bottom Sheet with Store Info
        StoreBottomSheet(
            store = selectedStore,
            isExpanded = isBottomSheetExpanded,
            onExpandToggle = { isBottomSheetExpanded = !isBottomSheetExpanded },
            onClose = { 
                selectedStore = null
                isBottomSheetExpanded = false
            },
            onShopNow = { store ->
                onStoreClick(store.id)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun MapTopBar(
    storeCount: Int,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onBackClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            modifier = Modifier.size(48.dp),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Nearby Stores",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            if (storeCount > 0) {
                Text(
                    text = "$storeCount stores found",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        Surface(
            onClick = onRefresh,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            modifier = Modifier.size(48.dp),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreBottomSheet(
    store: MapStore?,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onClose: () -> Unit,
    onShopNow: (MapStore) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = store != null,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        store?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 400.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Drag Handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(width = 48.dp, height = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Store Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = store.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = store.type,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = store.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Store Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StoreStatCard(
                            icon = Icons.Filled.Star,
                            value = "${store.rating}",
                            label = "Rating",
                            color = Color(0xFFFFC107)
                        )
                        StoreStatCard(
                            icon = Icons.Outlined.DirectionsCar,
                            value = store.distance.ifEmpty { "Nearby" },
                            label = "Distance",
                            color = MaterialTheme.colorScheme.primary
                        )
                        StoreStatCard(
                            icon = Icons.Outlined.Schedule,
                            value = "15-20 min",
                            label = "Delivery",
                            color = EcoColors.Green500
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onShopNow(store) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Store,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Shop Now")
                        }
                        
                        OutlinedButton(
                            onClick = { /* Show directions */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Directions,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Directions")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FallbackMapView(
    stores: List<MapStore>,
    isLoading: Boolean,
    onStoreClick: (MapStore) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nearby Stores",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (stores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Store,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No stores found nearby",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Try refreshing or expanding your search area",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onRefresh) {
                        Text("Refresh")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stores) { store ->
                    Card(
                        onClick = { onStoreClick(store) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = EcoColors.Green100,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Store,
                                        contentDescription = null,
                                        tint = EcoColors.Green500,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = store.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = store.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${store.rating}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (store.distance.isNotEmpty()) {
                                        Text(
                                            text = "• ${store.distance}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            Icon(
                                imageVector = Icons.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
