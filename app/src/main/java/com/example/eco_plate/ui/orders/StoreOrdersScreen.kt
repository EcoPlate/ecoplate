package com.example.eco_plate.ui.orders

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

// EcoColors from theme
private val EcoGreen600 = Color(0xFF16A34A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreOrdersScreen(
    viewModel: StoreOrdersViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedOrderForAction by remember { mutableStateOf<com.example.eco_plate.data.models.Order?>(null) }
    var showRefundDialog by remember { mutableStateOf(false) }
    var refundReason by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.updateResult.collectLatest { result ->
            when (result) {
                is Resource.Success -> {
                    snackbarHostState.showSnackbar("Order updated successfully")
                }
                is Resource.Error -> {
                    snackbarHostState.showSnackbar(result.message ?: "Update failed")
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Store Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EcoGreen600,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            StatusFilterChips(
                selectedStatus = selectedStatus,
                onStatusSelected = { viewModel.setStatusFilter(it) }
            )

            when (val ordersState = orders) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = EcoGreen600)
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                        EmptyStoreOrdersView(selectedStatus)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(ordersList) { order ->
                                StoreOrderCard(
                                    order = order,
                                    onUpdateStatus = { newStatus ->
                                        viewModel.updateOrderStatus(order.id, newStatus)
                                    },
                                    onRefund = {
                                        selectedOrderForAction = order
                                        showRefundDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRefundDialog && selectedOrderForAction != null) {
        AlertDialog(
            onDismissRequest = { 
                showRefundDialog = false
                selectedOrderForAction = null
                refundReason = ""
            },
            title = { Text("Refund Order") },
            text = {
                Column {
                    Text("Are you sure you want to refund order ${selectedOrderForAction?.orderNumber}?")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = refundReason,
                        onValueChange = { refundReason = it },
                        label = { Text("Reason (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedOrderForAction?.let {
                            viewModel.refundOrder(it.id, refundReason.takeIf { r -> r.isNotBlank() })
                        }
                        showRefundDialog = false
                        selectedOrderForAction = null
                        refundReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Refund")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRefundDialog = false
                    selectedOrderForAction = null
                    refundReason = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatusFilterChips(
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit
) {
    val statuses = listOf(
        null to "All",
        "PENDING" to "Pending",
        "CONFIRMED" to "Confirmed",
        "PREPARING" to "Preparing",
        "READY" to "Ready",
        "PICKED_UP" to "Picked Up",
        "DELIVERED" to "Delivered",
        "CANCELLED" to "Cancelled"
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statuses) { (status, label) ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EcoGreen600,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun EmptyStoreOrdersView(selectedStatus: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Inbox,
                null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Text(
                if (selectedStatus != null) "No ${selectedStatus.lowercase().replace("_", " ")} orders"
                else "No orders yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Text("Orders will appear here when customers place them", color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreOrderCard(
    order: com.example.eco_plate.data.models.Order,
    onUpdateStatus: (String) -> Unit,
    onRefund: () -> Unit
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        fontSize = 18.sp
                    )
                    Text(
                        formatDate(order.createdAt),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        order.status.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            order.items?.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${item.quantity}x ${item.itemSnapshot?.name ?: "Item"}",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "$${String.format("%.2f", item.total)}",
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", fontWeight = FontWeight.Bold)
                Text(
                    "$${String.format("%.2f", order.total)} CAD",
                    fontWeight = FontWeight.Bold,
                    color = EcoGreen600
                )
            }

            if (!order.customerNotes.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Note,
                            null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            order.customerNotes,
                            fontSize = 14.sp,
                            color = Color(0xFF795548)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when (order.status) {
                "PENDING" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onUpdateStatus("CANCELLED") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Decline")
                        }
                        Button(
                            onClick = { onUpdateStatus("CONFIRMED") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen600)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Accept")
                        }
                    }
                }
                "CONFIRMED" -> {
                    Button(
                        onClick = { onUpdateStatus("PREPARING") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                    ) {
                        Icon(Icons.Default.Restaurant, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Preparing")
                    }
                }
                "PREPARING" -> {
                    Button(
                        onClick = { onUpdateStatus("READY") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreen600)
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Mark as Ready")
                    }
                }
                "READY" -> {
                    Button(
                        onClick = { onUpdateStatus(if (order.isDelivery) "DELIVERED" else "PICKED_UP") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Done, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (order.isDelivery) "Mark Delivered" else "Mark Picked Up")
                    }
                }
                "PICKED_UP", "DELIVERED" -> {
                    if (order.paymentStatus != "refunded") {
                        OutlinedButton(
                            onClick = onRefund,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Undo, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Issue Refund")
                        }
                    } else {
                        Surface(
                            color = Color.Red.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color.Red)
                                Spacer(Modifier.width(8.dp))
                                Text("Refunded", color = Color.Red, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
                "CANCELLED" -> {
                    Surface(
                        color = Color.Red.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Cancel, null, tint = Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text("Order Cancelled", color = Color.Red, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}
