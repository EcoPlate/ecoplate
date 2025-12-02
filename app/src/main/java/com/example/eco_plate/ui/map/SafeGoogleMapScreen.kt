package com.example.eco_plate.ui.map

import android.Manifest
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    onBackClick: () -> Unit = {},
    onStoreClick: (String) -> Unit = {},
    onCallDriver: () -> Unit = {},
    onMessageDriver: () -> Unit = {},
    onReportIssue: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedStore by remember { mutableStateOf<MapStore?>(null) }
    var isBottomSheetExpanded by remember { mutableStateOf(false) }
    var mapLoadError by remember { mutableStateOf(false) }
    var showListView by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Observe ViewModel data
    val userLocation by viewModel.userLocation.observeAsState()
    val storesResource by viewModel.stores.observeAsState()
    val mapState by viewModel.mapState.observeAsState(MapState.Loading)
    val isLocationLoading by viewModel.isLocationLoading.observeAsState(true)
    val locationError by viewModel.locationError.observeAsState()
    
    // Permission state
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    // Request permission on launch if not granted
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else {
            // Permission granted, refresh location
            viewModel.loadUserLocation()
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
    
    // Show map state messages
    LaunchedEffect(mapState) {
        when (val state = mapState) {
            is MapState.Success -> {
                state.message?.let { message ->
                    if (message.contains("error", ignoreCase = true) || 
                        message.contains("No stores", ignoreCase = true)) {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            }
            is MapState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    actionLabel = if (state.canRetry) "Retry" else null,
                    duration = SnackbarDuration.Long
                ).let { result ->
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.retryLoadingStores()
                    }
                }
            }
            else -> {}
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
    
    // Function to open Google Maps for directions
    fun openDirections(store: MapStore) {
        val uri = Uri.parse("google.navigation:q=${store.location.latitude},${store.location.longitude}&mode=d")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser if Google Maps is not installed
            val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${store.location.latitude},${store.location.longitude}")
            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Show either map or list view based on toggle
        if (showListView || mapLoadError) {
            // List view of stores
            StoreListView(
                stores = stores,
                isLoading = isLoading,
                onStoreClick = { store ->
                    selectedStore = store
                    isBottomSheetExpanded = true
                },
                onNavigateToStore = { storeId ->
                    onStoreClick(storeId)
                },
                onRefresh = { viewModel.refreshStores() }
            )
        } else {
            // Map view
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
                // Current location marker
                Marker(
                    state = MarkerState(position = currentLocation),
                    title = "Your Location",
                    snippet = "You are here",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
                
                // Store markers from API - with improved visibility
                stores.forEach { store ->
                    val markerHue = when (store.type.uppercase()) {
                        "SUPERMARKET", "GROCERY", "GROCERY_STORE" -> BitmapDescriptorFactory.HUE_GREEN
                        "RESTAURANT" -> BitmapDescriptorFactory.HUE_ORANGE
                        "BAKERY" -> BitmapDescriptorFactory.HUE_YELLOW
                        "CAFE", "COFFEE" -> BitmapDescriptorFactory.HUE_CYAN
                        "DELI", "BUTCHER" -> BitmapDescriptorFactory.HUE_VIOLET
                        "PHARMACY", "DRUGSTORE" -> BitmapDescriptorFactory.HUE_ROSE
                        else -> BitmapDescriptorFactory.HUE_RED
                    }
                    
                    MarkerInfoWindowContent(
                        state = MarkerState(position = store.location),
                        title = store.name,
                        snippet = "${store.type} • ${store.rating}★ • ${store.distance}",
                        icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                        onClick = {
                            selectedStore = store
                            isBottomSheetExpanded = true
                            // Move camera to center on selected store
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLng(store.location),
                                    durationMs = 300
                                )
                            }
                            true // Return true to indicate we handled the click
                        }
                    ) {
                        // Custom info window content
                        StoreMarkerInfoWindow(
                            store = store,
                            onShopNow = { onStoreClick(store.id) }
                        )
                    }
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
        
        // Loading indicator
        if (isLoading && !showListView && !mapLoadError) {
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
        if (!showListView) {
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
        }
        
        // Top Bar
        MapTopBar(
            storeCount = stores.size,
            onBackClick = onBackClick,
            onRefresh = { viewModel.clearAndReload() },
            showListView = showListView,
            onToggleView = { showListView = !showListView },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        )
        
        // Map control buttons - only show when in map view
        if (!showListView) {
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
        }
        
        // "Search this area" button - shown when user pans/zooms the map
        AnimatedVisibility(
            visible = showSearchThisArea && !isLoading && !showListView,
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
        
        // Snackbar for errors
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (selectedStore != null && !showListView) 220.dp else 16.dp)
        )
        
        // Bottom Sheet with Store Info - only show in map view
        if (!showListView) {
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
                onGetDirections = { store ->
                    openDirections(store)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun MapTopBar(
    storeCount: Int,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    showListView: Boolean = false,
    onToggleView: () -> Unit = {},
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
                color = if (showListView) MaterialTheme.colorScheme.onSurface else Color.White,
                textAlign = TextAlign.Center
            )
            if (storeCount > 0) {
                Text(
                    text = "$storeCount stores found",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (showListView) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        // Toggle between map and list view
        Surface(
            onClick = onToggleView,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            modifier = Modifier.size(48.dp),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (showListView) Icons.Filled.Map else Icons.Filled.List,
                    contentDescription = if (showListView) "Show Map" else "Show List",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
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
    onGetDirections: (MapStore) -> Unit = {},
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = store.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                // Open/Closed indicator
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (store.isOpen) EcoColors.Green500.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (store.isOpen) "Open" else "Closed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (store.isOpen) EcoColors.Green500 else Color.Red,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = store.type,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                if (store.itemCount > 0) {
                                    Text(
                                        text = "${store.itemCount} items",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = store.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
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
                            value = String.format("%.1f", store.rating),
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
                            icon = Icons.Outlined.Inventory2,
                            value = if (store.itemCount > 0) "${store.itemCount}" else "N/A",
                            label = "Products",
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
                                containerColor = EcoColors.Green500
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Shop Now", fontWeight = FontWeight.Bold)
                        }
                        
                        OutlinedButton(
                            onClick = { onGetDirections(store) },
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

/**
 * Custom info window content for store markers
 */
@Composable
private fun StoreMarkerInfoWindow(
    store: MapStore,
    onShopNow: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .width(200.dp)
                .padding(12.dp)
        ) {
            Text(
                text = store.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = String.format("%.1f", store.rating),
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
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onShopNow,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoColors.Green500),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Shop Now", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/**
 * List view of stores - alternative to map view
 */
@Composable
private fun StoreListView(
    stores: List<MapStore>,
    isLoading: Boolean,
    onStoreClick: (MapStore) -> Unit,
    onNavigateToStore: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header spacer for top bar
        Spacer(modifier = Modifier.height(100.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = EcoColors.Green500)
                    Text(
                        text = "Finding nearby stores...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (stores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Store,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "No stores found nearby",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try refreshing or expanding your search area",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(containerColor = EcoColors.Green500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(stores) { store ->
                    StoreListItem(
                        store = store,
                        onStoreClick = { onStoreClick(store) },
                        onShopNow = { onNavigateToStore(store.id) }
                    )
                }
                
                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

/**
 * Store list item card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreListItem(
    store: MapStore,
    onStoreClick: () -> Unit,
    onShopNow: () -> Unit
) {
    Card(
        onClick = onStoreClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Store icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EcoColors.Green100,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Store,
                            contentDescription = null,
                            tint = EcoColors.Green500,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    // Store name with open status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = store.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (store.isOpen) EcoColors.Green500.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (store.isOpen) "Open" else "Closed",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (store.isOpen) EcoColors.Green500 else Color.Red,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Store type badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = store.type,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Address
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = store.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Stats row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = String.format("%.1f", store.rating),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Distance
                        if (store.distance.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DirectionsCar,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = store.distance,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Item count
                        if (store.itemCount > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${store.itemCount} items",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Shop Now button
            Button(
                onClick = onShopNow,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = EcoColors.Green500),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Shop Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}
