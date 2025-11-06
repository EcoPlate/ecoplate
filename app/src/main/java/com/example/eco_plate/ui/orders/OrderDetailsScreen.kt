package com.example.eco_plate.ui.orders

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eco_plate.ui.components.EcoColors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OrderDetailsInfo(
    val id: String,
    val status: DetailedOrderStatus,
    val storeName: String,
    val storeAddress: String,
    val deliveryAddress: String,
    val estimatedTime: String,
    val items: List<DetailedOrderItem>,
    val subtotal: Float,
    val deliveryFee: Float,
    val discount: Float,
    val total: Float,
    val driver: DriverInfo? = null,
    val trackingSteps: List<TrackingStep>
)

data class DetailedOrderItem(
    val name: String,
    val quantity: Int,
    val price: Float,
    val specialInstructions: String? = null
)

data class DriverInfo(
    val name: String,
    val rating: Float,
    val vehicleInfo: String,
    val phoneNumber: String,
    val profilePic: String? = null
)

data class TrackingStep(
    val title: String,
    val time: String?,
    val isCompleted: Boolean
)

enum class DetailedOrderStatus {
    PLACED, CONFIRMED, PREPARING, READY, PICKED_UP, ON_THE_WAY, DELIVERED
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: String,
    onBackClick: () -> Unit = {},
    onCallDriver: () -> Unit = {},
    onMessageDriver: () -> Unit = {},
    onReportIssue: () -> Unit = {}
) {
    // Sample order data
    val orderDetails = remember {
        OrderDetailsInfo(
            id = orderId,
            status = DetailedOrderStatus.ON_THE_WAY,
            storeName = "Whole Foods Market",
            storeAddress = "510 W 8th Ave, Vancouver",
            deliveryAddress = "1234 Main St, Vancouver",
            estimatedTime = "12:45 PM",
            items = listOf(
                DetailedOrderItem("Organic Avocados", 3, 5.99f),
                DetailedOrderItem("Whole Wheat Bread", 2, 4.99f),
                DetailedOrderItem("Almond Milk", 1, 3.99f, "Unsweetened"),
                DetailedOrderItem("Fresh Strawberries", 1, 6.99f)
            ),
            subtotal = 38.94f,
            deliveryFee = 2.99f,
            discount = 5.00f,
            total = 36.93f,
            driver = DriverInfo(
                name = "Michael Chen",
                rating = 4.9f,
                vehicleInfo = "Toyota Prius • ABC-1234",
                phoneNumber = "+1 (555) 123-4567"
            ),
            trackingSteps = listOf(
                TrackingStep("Order Placed", "12:15 PM", true),
                TrackingStep("Store Confirmed", "12:18 PM", true),
                TrackingStep("Preparing Order", "12:25 PM", true),
                TrackingStep("Driver Picked Up", "12:35 PM", true),
                TrackingStep("On the Way", "12:40 PM", true),
                TrackingStep("Delivered", null, false)
            )
        )
    }
    
    var showBottomSheet by remember { mutableStateOf(true) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val coroutineScope = rememberCoroutineScope()
    
    // Permission state
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    // Map locations
    val storeLocation = LatLng(49.2638, -123.1148) // Whole Foods
    val deliveryLocation = LatLng(49.2827, -123.1207) // Downtown Vancouver
    var driverLocation by remember { mutableStateOf(LatLng(49.2700, -123.1160)) }
    
    // Simulate driver movement
    LaunchedEffect(orderDetails.status) {
        if (orderDetails.status == DetailedOrderStatus.ON_THE_WAY) {
            while (true) {
                delay(3000)
                // Move driver closer to delivery
                driverLocation = LatLng(
                    driverLocation.latitude + (deliveryLocation.latitude - driverLocation.latitude) * 0.1,
                    driverLocation.longitude + (deliveryLocation.longitude - driverLocation.longitude) * 0.1
                )
            }
        }
    }
    
    // Camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(
                (storeLocation.latitude + deliveryLocation.latitude) / 2,
                (storeLocation.longitude + deliveryLocation.longitude) / 2
            ), 
            13f
        )
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Map View (Full screen background)
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = locationPermissionState.status.isGranted,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = false
            )
        ) {
            // Store Marker
            MarkerComposable(
                state = MarkerState(position = storeLocation),
                title = orderDetails.storeName
            ) {
                Surface(
                    shape = CircleShape,
                    color = EcoColors.Green500,
                    modifier = Modifier.size(40.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Store,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Delivery Location Marker
            MarkerComposable(
                state = MarkerState(position = deliveryLocation),
                title = "Delivery Location"
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Driver Marker
            if (orderDetails.driver != null) {
                MarkerComposable(
                    state = MarkerState(position = driverLocation),
                    title = orderDetails.driver.name
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.size(48.dp),
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.DirectionsCar,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
            
            // Route Polyline
            Polyline(
                points = listOf(storeLocation, driverLocation, deliveryLocation),
                color = EcoColors.Green500,
                width = 10f
            )
        }
        
        // Top Bar with improved visibility
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
            shadowElevation = 8.dp
        ) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Order #${orderDetails.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getStatusText(orderDetails.status),
                            style = MaterialTheme.typography.bodySmall,
                            color = EcoColors.Green600
                        )
                    }
                },
                navigationIcon = {
                    Surface(
                        onClick = onBackClick,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp),
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.ArrowBack, 
                                "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onReportIssue) {
                        Icon(Icons.Outlined.Info, "Help")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
        
        // Bottom Sheet with Order Details
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showBottomSheet = false
                    onBackClick() 
                },
                sheetState = bottomSheetState,
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(width = 48.dp, height = 4.dp)
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Estimated Time
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = EcoColors.Green50
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Estimated arrival",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = orderDetails.estimatedTime,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EcoColors.Green600
                                )
                            }
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = EcoColors.Green600,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                // Driver Info (if assigned)
                orderDetails.driver?.let { driver ->
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        DriverInfoCard(
                            driver = driver,
                            onCall = onCallDriver,
                            onMessage = onMessageDriver,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                
                // Tracking Steps
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TrackingSection(
                        steps = orderDetails.trackingSteps,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                // Order Items
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OrderItemsSection(
                        items = orderDetails.items,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                // Order Summary
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OrderSummarySection(
                        subtotal = orderDetails.subtotal,
                        deliveryFee = orderDetails.deliveryFee,
                        discount = orderDetails.discount,
                        total = orderDetails.total,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                // Store Info
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    StoreInfoSection(
                        storeName = orderDetails.storeName,
                        storeAddress = orderDetails.storeAddress,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
        }  // End of if (showBottomSheet)
    }
}

@Composable
private fun DriverInfoCard(
    driver: DriverInfo,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Driver Avatar
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = driver.name.split(" ").map { it.first() }.joinToString(""),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            text = driver.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${driver.rating}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = driver.vehicleInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = onCall,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Call,
                            contentDescription = "Call",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onMessage,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Message,
                            contentDescription = "Message",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackingSection(
    steps: List<TrackingStep>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Order Tracking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Step Indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (step.isCompleted) EcoColors.Green500 else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(24.dp)
                        ) {
                            if (step.isCompleted) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        
                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(40.dp)
                                    .background(
                                        if (step.isCompleted) EcoColors.Green500 
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }
                    }
                    
                    // Step Info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = if (index < steps.size - 1) 16.dp else 0.dp)
                    ) {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (step.isCompleted) FontWeight.Medium else FontWeight.Normal,
                            color = if (step.isCompleted) MaterialTheme.colorScheme.onSurface 
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        step.time?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemsSection(
    items: List<DetailedOrderItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Order Items",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${item.quantity}x ${item.name}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        item.specialInstructions?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "$${String.format("%.2f", item.price * item.quantity)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderSummarySection(
    subtotal: Float,
    deliveryFee: Float,
    discount: Float,
    total: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", style = MaterialTheme.typography.bodyLarge)
                Text("$${String.format("%.2f", subtotal)}", style = MaterialTheme.typography.bodyLarge)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Delivery Fee", style = MaterialTheme.typography.bodyLarge)
                Text("$${String.format("%.2f", deliveryFee)}", style = MaterialTheme.typography.bodyLarge)
            }
            
            if (discount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Discount", style = MaterialTheme.typography.bodyLarge, color = EcoColors.Green600)
                    Text("-$${String.format("%.2f", discount)}", style = MaterialTheme.typography.bodyLarge, color = EcoColors.Green600)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("$${String.format("%.2f", total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StoreInfoSection(
    storeName: String,
    storeAddress: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Store Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Outlined.Store,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = storeName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = storeAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getStatusText(status: DetailedOrderStatus): String {
    return when (status) {
        DetailedOrderStatus.PLACED -> "Order Placed"
        DetailedOrderStatus.CONFIRMED -> "Order Confirmed"
        DetailedOrderStatus.PREPARING -> "Preparing Your Order"
        DetailedOrderStatus.READY -> "Order Ready"
        DetailedOrderStatus.PICKED_UP -> "Driver Picked Up"
        DetailedOrderStatus.ON_THE_WAY -> "On the Way to You"
        DetailedOrderStatus.DELIVERED -> "Delivered"
    }
}
