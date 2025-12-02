package com.example.eco_plate.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eco_plate.data.models.Order
import com.example.eco_plate.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

private val EcoGreen600 = Color(0xFF16A34A)
private val EcoGreen50 = Color(0xFFF0FDF4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onOrderClick: (String) -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EcoGreen600,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        when (val ordersState = orders) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EcoGreen600)
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(ordersState.message ?: "Error loading orders")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is Resource.Success -> {
                val ordersList = ordersState.data ?: emptyList()
                if (ordersList.isEmpty()) {
                    EmptyOrdersView()
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ordersList) { order ->
                            OrderCard(
                                order = order,
                                onClick = { onOrderClick(order.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyOrdersView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Receipt,
                null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Text("No orders yet", fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text("Your order history will appear here", color = Color.Gray)
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    val statusColor = when (order.status) {
        "PENDING" -> Color(0xFFFF9800)
        "CONFIRMED" -> Color(0xFF2196F3)
        "PREPARING" -> Color(0xFF9C27B0)
        "READY" -> EcoGreen600
        "PICKED_UP", "DELIVERED" -> Color(0xFF4CAF50)
        "CANCELLED" -> Color.Red
        else -> Color.Gray
    }

    val statusIcon = when (order.status) {
        "PENDING" -> Icons.Default.Schedule
        "CONFIRMED" -> Icons.Default.CheckCircle
        "PREPARING" -> Icons.Default.Restaurant
        "READY" -> Icons.Default.LocalShipping
        "PICKED_UP", "DELIVERED" -> Icons.Default.Done
        "CANCELLED" -> Icons.Default.Cancel
        else -> Icons.Default.Receipt
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        order.orderNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        order.store?.name ?: "Store",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            statusIcon,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = statusColor
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            formatStatus(order.status),
                            color = statusColor,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            order.items?.take(3)?.forEach { item ->
                Text(
                    "${item.quantity}x ${item.itemSnapshot?.name ?: "Item"}",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if ((order.items?.size ?: 0) > 3) {
                Text(
                    "+${(order.items?.size ?: 0) - 3} more items",
                    color = EcoGreen600,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatDate(order.createdAt),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    "$${String.format("%.2f", order.total)} CAD",
                    fontWeight = FontWeight.Bold,
                    color = EcoGreen600
                )
            }
        }
    }
}

private fun formatStatus(status: String): String {
    return status.replace("_", " ")
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    viewModel: OrdersViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val selectedOrder by viewModel.selectedOrder.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadOrderDetails(orderId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearSelectedOrder() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EcoGreen600,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        when (val orderState = selectedOrder) {
            null, is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EcoGreen600)
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, null, tint = Color.Red)
                        Spacer(Modifier.height(8.dp))
                        Text(orderState.message ?: "Error loading order")
                    }
                }
            }
            is Resource.Success -> {
                orderState.data?.let { order ->
                    OrderDetailContent(
                        order = order,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderDetailContent(
    order: Order,
    modifier: Modifier = Modifier
) {
    val statusColor = when (order.status) {
        "PENDING" -> Color(0xFFFF9800)
        "CONFIRMED" -> Color(0xFF2196F3)
        "PREPARING" -> Color(0xFF9C27B0)
        "READY" -> EcoGreen600
        "PICKED_UP", "DELIVERED" -> Color(0xFF4CAF50)
        "CANCELLED" -> Color.Red
        else -> Color.Gray
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(statusColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (order.status) {
                                "PENDING" -> Icons.Default.Schedule
                                "CONFIRMED" -> Icons.Default.CheckCircle
                                "PREPARING" -> Icons.Default.Restaurant
                                "READY" -> Icons.Default.LocalShipping
                                else -> Icons.Default.Done
                            },
                            null,
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            formatStatus(order.status),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = statusColor
                        )
                        Text(
                            "Order ${order.orderNumber}",
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Store,
                        null,
                        tint = EcoGreen600,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            order.store?.name ?: "Store",
                            fontWeight = FontWeight.Bold
                        )
                        order.store?.address?.let {
                            Text(it, color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order Items", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))

                    order.items?.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${item.quantity}x",
                                    fontWeight = FontWeight.Bold,
                                    color = EcoGreen600
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    item.itemSnapshot?.name ?: "Item",
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                "$${String.format("%.2f", item.total)}",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = EcoGreen50)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))

                    SummaryRow("Subtotal", order.subtotal)
                    if (order.discount > 0) {
                        SummaryRow("Discount", -order.discount)
                    }
                    SummaryRow("Tax", order.tax)
                    if (order.tip > 0) {
                        SummaryRow("Tip", order.tip)
                    }
                    if (order.deliveryFee > 0) {
                        SummaryRow("Delivery Fee", order.deliveryFee)
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "$${String.format("%.2f", order.total)} CAD",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = EcoGreen600
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    
                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Paid via ${order.paymentStatus ?: "Stripe"}",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        if (!order.customerNotes.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Your Notes", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(order.customerNotes, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(
            "${if (amount < 0) "-" else ""}$${String.format("%.2f", kotlin.math.abs(amount))}",
            color = if (amount < 0) EcoGreen600 else Color.Black
        )
    }
}
