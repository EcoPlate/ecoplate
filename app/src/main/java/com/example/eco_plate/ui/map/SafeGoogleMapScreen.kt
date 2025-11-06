package com.example.eco_plate.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.eco_plate.ui.components.EcoColors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Vancouver area stores with real coordinates
data class StoreLocation(
    val id: String,
    val name: String,
    val address: String,
    val location: LatLng,
    val type: String,
    val rating: Float = 4.5f,
    val distance: String = "1.2 km"
)

// Sample stores around Vancouver
val vancouverStores = listOf(
    StoreLocation("1", "Whole Foods Market", "510 W 8th Ave, Vancouver", LatLng(49.2638, -123.1148), "Grocery"),
    StoreLocation("2", "Trader Joe's", "2205 W 4th Ave, Vancouver", LatLng(49.2685, -123.1570), "Grocery"),
    StoreLocation("3", "Safeway Broadway", "1766 W Broadway, Vancouver", LatLng(49.2635, -123.1425), "Grocery"),
    StoreLocation("4", "Save-On-Foods", "2308 Cambie St, Vancouver", LatLng(49.2650, -123.1149), "Grocery"),
    StoreLocation("5", "IGA Marketplace", "909 Burrard St, Vancouver", LatLng(49.2815, -123.1248), "Grocery"),
    StoreLocation("6", "T&T Supermarket", "179 Keefer Pl, Vancouver", LatLng(49.2794, -123.1087), "Asian Grocery"),
    StoreLocation("7", "Fresh St. Market", "1650 Davie St, Vancouver", LatLng(49.2859, -123.1374), "Specialty"),
    StoreLocation("8", "Urban Fare", "305 Bute St, Vancouver", LatLng(49.2863, -123.1317), "Premium Grocery"),
    StoreLocation("9", "Choices Markets", "1888 W 1st Ave, Vancouver", LatLng(49.2708, -123.1480), "Organic"),
    StoreLocation("10", "Nesters Market", "990 Seymour St, Vancouver", LatLng(49.2781, -123.1192), "Local")
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SafeGoogleMapScreen(
    onBackClick: () -> Unit = {},
    onCallDriver: () -> Unit = {},
    onMessageDriver: () -> Unit = {},
    onReportIssue: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedStore by remember { mutableStateOf<StoreLocation?>(null) }
    var isBottomSheetExpanded by remember { mutableStateOf(false) }
    var mapLoadError by remember { mutableStateOf(false) }
    
    // Permission state
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    // Default location (Downtown Vancouver)
    val defaultLocation = LatLng(49.2827, -123.1207)
    var currentLocation by remember { mutableStateOf(defaultLocation) }
    
    // Simulate driver location
    val driverLocation = remember {
        val randomStore = vancouverStores.random()
        LatLng(
            randomStore.location.latitude + Random.nextDouble(-0.01, 0.01),
            randomStore.location.longitude + Random.nextDouble(-0.01, 0.01)
        )
    }
    
    // Camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 13f)
    }
    
    // Map properties
    val mapProperties by remember {
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
                    // Current location marker
                    Marker(
                        state = MarkerState(position = currentLocation),
                        title = "Your Location",
                        snippet = "Current delivery address",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                    
                    // Store markers
                    vancouverStores.forEach { store ->
                        Marker(
                            state = MarkerState(position = store.location),
                            title = store.name,
                            snippet = "${store.type} • ${store.rating}★ • ${store.distance}",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                when (store.type) {
                                    "Grocery" -> BitmapDescriptorFactory.HUE_GREEN
                                    "Asian Grocery" -> BitmapDescriptorFactory.HUE_YELLOW
                                    "Premium Grocery" -> BitmapDescriptorFactory.HUE_VIOLET
                                    "Organic" -> BitmapDescriptorFactory.HUE_CYAN
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
                    
                    // Driver marker (if showing delivery tracking)
                    Marker(
                        state = MarkerState(position = driverLocation),
                        title = "Driver",
                        snippet = "On the way",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                        rotation = 45f
                    )
                    
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
                stores = vancouverStores,
                onStoreClick = { store ->
                    selectedStore = store
                    isBottomSheetExpanded = true
                }
            )
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
            onBackClick = onBackClick,
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
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(currentLocation, 15f)
                            )
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
        
        // Bottom Sheet with Store/Delivery Info
        StoreBottomSheet(
            store = selectedStore,
            isExpanded = isBottomSheetExpanded,
            onExpandToggle = { isBottomSheetExpanded = !isBottomSheetExpanded },
            onClose = { 
                selectedStore = null
                isBottomSheetExpanded = false
            },
            onCallDriver = onCallDriver,
            onMessageDriver = onMessageDriver,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun MapTopBar(
    onBackClick: () -> Unit,
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
        
        Text(
            text = "Store Locations",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreBottomSheet(
    store: StoreLocation?,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onClose: () -> Unit,
    onCallDriver: () -> Unit,
    onMessageDriver: () -> Unit,
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
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = store.type,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = store.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            value = store.distance,
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
                            onClick = { /* Navigate to store */ },
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
            fontWeight = FontWeight.Bold
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
    stores: List<StoreLocation>,
    onStoreClick: (StoreLocation) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Nearby Stores",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 60.dp, bottom = 8.dp)
        )
        
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
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = store.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                Text(
                                    text = "• ${store.distance}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
