package com.example.eco_plate.ui.checkout

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.eco_plate.data.models.*
import com.example.eco_plate.utils.Resource
import com.stripe.android.paymentsheet.PaymentSheetContract

// Theme colors
private val EcoGreen600 = Color(0xFF16A34A)
private val EcoGreen50 = Color(0xFFF0FDF4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onOrderSuccess: (List<Order>) -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()
    val tipAmount by viewModel.tipAmount.collectAsState()
    val customerNotes by viewModel.customerNotes.collectAsState()

    val paymentSheetLauncher = rememberLauncherForActivityResult(
        contract = PaymentSheetContract()
    ) { result ->
        viewModel.handlePaymentResult(result)
    }

    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is PaymentState.ReadyToPay -> {
                paymentSheetLauncher.launch(
                    PaymentSheetContract.Args.createPaymentIntentArgs(state.clientSecret)
                )
            }
            is PaymentState.Success -> {
                onOrderSuccess(state.orders)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
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
        when (val cartState = cart) {
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
                        Text(cartState.message ?: "Error loading cart")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadCart() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is Resource.Success -> {
                val cartData = cartState.data
                if (cartData == null || cartData.items.isEmpty()) {
                    EmptyCartView(onBackClick)
                } else {
                    CheckoutContent(
                        cart = cartData,
                        tipAmount = tipAmount,
                        customerNotes = customerNotes,
                        paymentState = paymentState,
                        onTipChange = { viewModel.setTipAmount(it) },
                        onNotesChange = { viewModel.setCustomerNotes(it) },
                        onUpdateQuantity = { itemId, qty -> viewModel.updateCartItem(itemId, qty) },
                        onRemoveItem = { viewModel.removeFromCart(it) },
                        onCheckout = { viewModel.createPaymentIntent() },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        if (paymentState is PaymentState.Loading || paymentState is PaymentState.Processing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = EcoGreen600)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (paymentState is PaymentState.Processing) "Processing payment..." else "Preparing checkout...",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (paymentState is PaymentState.Error) {
            AlertDialog(
                onDismissRequest = { viewModel.resetPaymentState() },
                title = { Text("Payment Error") },
                text = { Text((paymentState as PaymentState.Error).message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetPaymentState() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun EmptyCartView(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ShoppingCart,
                null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Text("Your cart is empty", fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text("Add some items to get started", color = Color.Gray)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen600)
            ) {
                Text("Browse Products")
            }
        }
    }
}

@Composable
private fun CheckoutContent(
    cart: Cart,
    tipAmount: Double,
    customerNotes: String,
    paymentState: PaymentState,
    onTipChange: (Double) -> Unit,
    onNotesChange: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalWithTip = cart.total + tipAmount

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val itemsByStore = cart.items.groupBy { it.item?.store?.name ?: "Unknown Store" }
            
            itemsByStore.forEach { (storeName, items) ->
                item {
                    Text(
                        storeName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(items) { cartItem ->
                    CartItemCard(
                        cartItem = cartItem,
                        onUpdateQuantity = { qty -> onUpdateQuantity(cartItem.itemId, qty) },
                        onRemove = { onRemoveItem(cartItem.itemId) }
                    )
                }
            }

            item {
                TipSection(tipAmount = tipAmount, onTipChange = onTipChange)
            }

            item {
                NotesSection(notes = customerNotes, onNotesChange = onNotesChange)
            }

            item {
                OrderSummaryCard(
                    subtotal = cart.subtotal,
                    tax = cart.tax,
                    discount = cart.discount,
                    tip = tipAmount,
                    total = totalWithTip
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen600),
                shape = RoundedCornerShape(12.dp),
                enabled = paymentState !is PaymentState.Loading && paymentState !is PaymentState.Processing
            ) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Pay $${String.format("%.2f", totalWithTip)} CAD",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CartItemCard(
    cartItem: CartItem,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val item = cartItem.item ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.images.firstOrNull() ?: "",
                contentDescription = item.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "$${String.format("%.2f", cartItem.price)}",
                    color = EcoGreen600,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { 
                        if (cartItem.quantity > 1) onUpdateQuantity(cartItem.quantity - 1) 
                        else onRemove()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        if (cartItem.quantity > 1) Icons.Default.Remove else Icons.Default.Delete,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    "${cartItem.quantity}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { onUpdateQuantity(cartItem.quantity + 1) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(EcoGreen600, CircleShape)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TipSection(
    tipAmount: Double,
    onTipChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Add a tip", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.0, 1.0, 2.0, 5.0).forEach { amount ->
                    FilterChip(
                        selected = tipAmount == amount,
                        onClick = { onTipChange(amount) },
                        label = { 
                            Text(
                                if (amount == 0.0) "No tip" else "$${amount.toInt()}",
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EcoGreen600,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            
            var customTip by remember { mutableStateOf("") }
            OutlinedTextField(
                value = customTip,
                onValueChange = { 
                    customTip = it
                    it.toDoubleOrNull()?.let { amt -> onTipChange(amt) }
                },
                label = { Text("Custom amount") },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun NotesSection(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order notes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { Text("Any special instructions?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
        }
    }
}

@Composable
private fun OrderSummaryCard(
    subtotal: Double,
    tax: Double,
    discount: Double,
    tip: Double,
    total: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EcoGreen50)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            SummaryRow("Subtotal", subtotal)
            if (discount > 0) {
                SummaryRow("Discount", -discount, isDiscount = true)
            }
            SummaryRow("Tax (12% HST)", tax)
            if (tip > 0) {
                SummaryRow("Tip", tip)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "$${String.format("%.2f", total)} CAD",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = EcoGreen600
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    amount: Double,
    isDiscount: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(
            "${if (isDiscount) "-" else ""}$${String.format("%.2f", kotlin.math.abs(amount))}",
            color = if (isDiscount) EcoGreen600 else Color.Black
        )
    }
}
