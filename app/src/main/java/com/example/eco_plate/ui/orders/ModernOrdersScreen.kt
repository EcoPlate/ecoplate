package com.example.eco_plate.ui.orders

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.eco_plate.R
import com.example.eco_plate.ui.components.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}

data class OrderItem(
    val name: String,
    val quantity: Int,
    val price: Float,
    val image: String? = null
)

data class Order(
    val id: String,
    val orderNumber: String,
    val storeName: String,
    val storeImage: String? = null,
    val items: List<OrderItem>,
    val totalAmount: Float,
    val totalSaved: Float,
    val status: OrderStatus,
    val orderDate: Date,
    val estimatedDelivery: String? = null,
    val deliveryAddress: String,
    val paymentMethod: String = "•••• 1234"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernOrdersScreen(
    onNavigateToOrderDetail: (String) -> Unit = {},
    onNavigateToReorder: (String) -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onTrackOrder: (String) -> Unit = { onNavigateToOrderDetail(it) } // Track order navigates to detail/map
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf("Active", "Past Orders", "Cancelled")
    
    // Sample orders data
    val activeOrders = remember {
        listOf(
            Order(
                id = "1",
                orderNumber = "ECO-2024-001",
                storeName = "Whole Foods Market",
                items = listOf(
                    OrderItem("Organic Avocados", 2, 4.49f),
                    OrderItem("Fresh Strawberries", 1, 3.49f),
                    OrderItem("Sourdough Bread", 1, 2.49f),
                    OrderItem("Almond Milk", 2, 3.99f),
                    OrderItem("Free Range Eggs", 1, 5.99f)
                ),
                totalAmount = 26.94f,
                totalSaved = 12.50f,
                status = OrderStatus.OUT_FOR_DELIVERY,
                orderDate = Date(),
                estimatedDelivery = "2:30 PM - 2:45 PM",
                deliveryAddress = "789 Oak Street, Vancouver"
            ),
            Order(
                id = "2",
                orderNumber = "ECO-2024-002",
                storeName = "Trader Joe's",
                items = listOf(
                    OrderItem("Mixed Vegetables", 3, 5.99f),
                    OrderItem("Greek Yogurt", 2, 3.99f),
                    OrderItem("Quinoa Salad", 1, 7.99f)
                ),
                totalAmount = 33.94f,
                totalSaved = 15.00f,
                status = OrderStatus.PREPARING,
                orderDate = Date(),
                estimatedDelivery = "3:00 PM - 3:30 PM",
                deliveryAddress = "456 Maple Drive, Vancouver"
            ),
            Order(
                id = "3",
                orderNumber = "ECO-2024-003",
                storeName = "Save-On-Foods",
                items = listOf(
                    OrderItem("Fresh Salmon", 1, 12.99f),
                    OrderItem("Asparagus Bundle", 1, 4.99f),
                    OrderItem("Brown Rice", 1, 3.49f),
                    OrderItem("Lemon", 2, 0.79f)
                ),
                totalAmount = 23.05f,
                totalSaved = 8.00f,
                status = OrderStatus.CONFIRMED,
                orderDate = Date(),
                estimatedDelivery = "4:00 PM - 4:30 PM",
                deliveryAddress = "321 Pine Street, Vancouver"
            )
        )
    }
    
    val pastOrders = remember {
        listOf(
            Order(
                id = "4",
                orderNumber = "ECO-2024-004",
                storeName = "Safeway",
                items = listOf(
                    OrderItem("Milk (2L)", 1, 2.99f),
                    OrderItem("Eggs (12 pack)", 1, 3.49f),
                    OrderItem("Whole Wheat Bread", 2, 2.49f),
                    OrderItem("Bananas", 6, 1.99f),
                    OrderItem("Ground Coffee", 1, 8.99f)
                ),
                totalAmount = 22.44f,
                totalSaved = 9.50f,
                status = OrderStatus.DELIVERED,
                orderDate = Date(System.currentTimeMillis() - 86400000), // Yesterday
                deliveryAddress = "123 Main Street, Vancouver"
            ),
            Order(
                id = "5",
                orderNumber = "ECO-2024-005",
                storeName = "Urban Fare",
                items = listOf(
                    OrderItem("Artisan Cheese", 1, 15.99f),
                    OrderItem("Prosciutto", 1, 12.99f),
                    OrderItem("Olive Oil", 1, 18.99f),
                    OrderItem("Fresh Pasta", 2, 7.99f)
                ),
                totalAmount = 63.95f,
                totalSaved = 18.00f,
                status = OrderStatus.DELIVERED,
                orderDate = Date(System.currentTimeMillis() - 172800000), // 2 days ago
                deliveryAddress = "555 Beach Avenue, Vancouver"
            ),
            Order(
                id = "6",
                orderNumber = "ECO-2024-006",
                storeName = "T&T Supermarket",
                items = listOf(
                    OrderItem("Sushi Grade Tuna", 1, 24.99f),
                    OrderItem("Soy Sauce", 1, 3.49f),
                    OrderItem("Wasabi", 1, 2.99f),
                    OrderItem("Nori Sheets", 1, 4.99f),
                    OrderItem("Sushi Rice", 1, 6.99f)
                ),
                totalAmount = 43.45f,
                totalSaved = 12.00f,
                status = OrderStatus.DELIVERED,
                orderDate = Date(System.currentTimeMillis() - 259200000), // 3 days ago
                deliveryAddress = "888 Granville Street, Vancouver"
            ),
            Order(
                id = "7",
                orderNumber = "ECO-2024-007",
                storeName = "Fresh St. Market",
                items = listOf(
                    OrderItem("Organic Kale", 2, 3.99f),
                    OrderItem("Coconut Water", 6, 2.49f),
                    OrderItem("Protein Powder", 1, 34.99f),
                    OrderItem("Chia Seeds", 1, 8.99f)
                ),
                totalAmount = 66.90f,
                totalSaved = 20.00f,
                status = OrderStatus.DELIVERED,
                orderDate = Date(System.currentTimeMillis() - 345600000), // 4 days ago
                deliveryAddress = "999 Davie Street, Vancouver"
            )
        )
    }
    
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            OrdersTopBar(onSupportClick = onNavigateToSupport)
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            // Content based on selected tab
            when (selectedTab) {
                0 -> ActiveOrdersContent(
                    orders = activeOrders,
                    onOrderClick = onNavigateToOrderDetail,
                    onTrackOrder = onTrackOrder
                )
                1 -> PastOrdersContent(
                    orders = pastOrders,
                    onOrderClick = onNavigateToOrderDetail,
                    onReorder = onNavigateToReorder
                )
                2 -> EmptyOrdersView(
                    message = "No cancelled orders",
                    description = "Orders you've cancelled will appear here"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersTopBar(
    onSupportClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Your Orders",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(onClick = onSupportClick) {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = "Support",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun ActiveOrdersContent(
    orders: List<Order>,
    onOrderClick: (String) -> Unit,
    onTrackOrder: (String) -> Unit
) {
    if (orders.isEmpty()) {
        EmptyOrdersView(
            message = "No active orders",
            description = "Your current orders will appear here"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(orders) { order ->
                ActiveOrderCard(
                    order = order,
                    onClick = { onOrderClick(order.id) },
                    onTrackOrder = { onTrackOrder(order.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveOrderCard(
    order: Order,
    onClick: () -> Unit,
    onTrackOrder: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header with store info and status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        when (order.status) {
                            OrderStatus.OUT_FOR_DELIVERY -> EcoColors.Green100
                            OrderStatus.PREPARING -> EcoColors.Orange500.copy(alpha = 0.1f)
                            OrderStatus.CONFIRMED -> EcoColors.Blue500.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Store image
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        AsyncImage(
                            model = order.storeImage ?: R.drawable.ic_launcher_background,
                            contentDescription = order.storeName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Column {
                        Text(
                            text = order.storeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Order #${order.orderNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                OrderStatusBadge(status = order.status)
            }
            
            // Order progress
            if (order.status in listOf(OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.OUT_FOR_DELIVERY)) {
                OrderProgressIndicator(
                    status = order.status,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            
            // Estimated delivery
            order.estimatedDelivery?.let { time ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Estimated delivery: $time",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            
            // Items summary
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${order.items.size} items • $${String.format("%.2f", order.totalAmount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // First few items
                order.items.take(2).forEach { item ->
                    Text(
                        text = "${item.quantity}x ${item.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (order.items.size > 2) {
                    Text(
                        text = "and ${order.items.size - 2} more...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (order.status == OrderStatus.OUT_FOR_DELIVERY) {
                    Button(
                        onClick = onTrackOrder,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Track Order")
                    }
                }
                
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Details")
                }
            }
        }
    }
}

@Composable
private fun OrderStatusBadge(status: OrderStatus) {
    val (text, backgroundColor, contentColor) = when (status) {
        OrderStatus.PENDING -> Triple("Pending", EcoColors.Gray200, Color.Gray)
        OrderStatus.CONFIRMED -> Triple("Confirmed", EcoColors.Blue500.copy(alpha = 0.1f), EcoColors.Blue500)
        OrderStatus.PREPARING -> Triple("Preparing", EcoColors.Orange500.copy(alpha = 0.1f), EcoColors.Orange500)
        OrderStatus.READY_FOR_PICKUP -> Triple("Ready", EcoColors.Green500.copy(alpha = 0.1f), EcoColors.Green500)
        OrderStatus.OUT_FOR_DELIVERY -> Triple("On the way", EcoColors.Green500.copy(alpha = 0.1f), EcoColors.Green500)
        OrderStatus.DELIVERED -> Triple("Delivered", EcoColors.Green500.copy(alpha = 0.1f), EcoColors.Green500)
        OrderStatus.CANCELLED -> Triple("Cancelled", EcoColors.Red500.copy(alpha = 0.1f), EcoColors.Red500)
    }
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (status == OrderStatus.OUT_FOR_DELIVERY) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val animatedAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_alpha"
                )
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            contentColor.copy(alpha = animatedAlpha),
                            CircleShape
                        )
                )
            }
            
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun OrderProgressIndicator(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        Pair("Confirmed", OrderStatus.CONFIRMED),
        Pair("Preparing", OrderStatus.PREPARING),
        Pair("On the way", OrderStatus.OUT_FOR_DELIVERY),
        Pair("Delivered", OrderStatus.DELIVERED)
    )
    
    val currentStep = when (status) {
        OrderStatus.CONFIRMED -> 0
        OrderStatus.PREPARING -> 1
        OrderStatus.OUT_FOR_DELIVERY -> 2
        OrderStatus.DELIVERED -> 3
        else -> -1
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (label, _) ->
            val isCompleted = index <= currentStep
            val isActive = index == currentStep
            
            // Step indicator
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = when {
                        isActive -> MaterialTheme.colorScheme.primary
                        isCompleted -> EcoColors.Green500
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(if (isActive) 24.dp else 20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted && !isActive) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        } else if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, CircleShape)
                            )
                        }
                    }
                }
            }
            
            // Line connector
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (index < currentStep) EcoColors.Green500
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun PastOrdersContent(
    orders: List<Order>,
    onOrderClick: (String) -> Unit,
    onReorder: (String) -> Unit
) {
    if (orders.isEmpty()) {
        EmptyOrdersView(
            message = "No past orders",
            description = "Your order history will appear here"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(orders) { order ->
                PastOrderCard(
                    order = order,
                    onClick = { onOrderClick(order.id) },
                    onReorder = { onReorder(order.id) }
                )
            }
        }
    }
}

@Composable
private fun PastOrderCard(
    order: Order,
    onClick: () -> Unit,
    onReorder: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Store image
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(60.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        AsyncImage(
                            model = order.storeImage ?: R.drawable.ic_launcher_background,
                            contentDescription = order.storeName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = order.storeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(order.orderDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = EcoColors.Green500,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Delivered",
                                style = MaterialTheme.typography.bodySmall,
                                color = EcoColors.Green500,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            
            // Items
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                order.items.take(2).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity}x ${item.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$${String.format("%.2f", item.price * item.quantity)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (order.items.size > 2) {
                    Text(
                        text = "and ${order.items.size - 2} more items...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Total and savings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total: $${String.format("%.2f", order.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (order.totalSaved > 0) {
                        Text(
                            text = "You saved $${String.format("%.2f", order.totalSaved)}!",
                            style = MaterialTheme.typography.bodySmall,
                            color = EcoColors.Green500,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Button(
                    onClick = onReorder,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reorder")
                }
            }
        }
    }
}

@Composable
private fun EmptyOrdersView(
    message: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { /* Navigate to home */ },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Start Shopping")
        }
    }
}
