package com.example.eco_plate.ui.cart

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import com.example.eco_plate.R
import com.example.eco_plate.ui.components.*
import com.example.eco_plate.data.repository.CartItem as RepoCartItem
import com.example.eco_plate.data.repository.CartStore as RepoCartStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// UI-specific CartItem for display
data class CartItem(
    val id: String,
    val itemId: String, // Backend item/product ID for API calls
    val name: String,
    val storeName: String,
    val image: String? = null,
    val originalPrice: Float,
    val discountedPrice: Float,
    val quantity: Int,
    val expiryDate: String
)

// UI-specific CartStore for display
data class CartStore(
    val storeName: String,
    val items: List<CartItem>,
    val deliveryFee: Float,
    val estimatedTime: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCartScreen(
    viewModel: CartViewModel = viewModel(),
    onNavigateToCheckout: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val cartStoresState by viewModel.cartStores.collectAsState()
    
    // Convert the data from repository to screen's expected format
    val cartStores = remember(cartStoresState) {
        cartStoresState.map { store ->
            CartStore(
                storeName = store.storeName,
                items = store.items.map { item ->
                    CartItem(
                        id = item.id,
                        itemId = item.itemId.ifEmpty { item.id }, // Use itemId for API calls
                        name = item.name,
                        storeName = item.storeName,
                        image = item.imageUrl,
                        originalPrice = item.originalPrice ?: (item.price * 1.5f), // Use original price or estimate
                        discountedPrice = item.price,
                        quantity = item.quantity,
                        expiryDate = item.expiryDate ?: "Today" // Use expiry date or default
                    )
                },
                deliveryFee = store.deliveryFee,
                estimatedTime = store.deliveryTime
            )
        }
    }
    
    var promoCode by remember { mutableStateOf("") }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Your Cart",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartStores.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Clear All", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (cartStores.isNotEmpty()) {
                CartBottomBar(
                    cartStores = cartStores,
                    onCheckout = onNavigateToCheckout
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        if (cartStores.isEmpty()) {
            EmptyCartView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                contentPadding = PaddingValues(vertical = Spacing.md)
            ) {
                // Cart items grouped by store
                cartStoresState.forEachIndexed { index, store ->
                    item {
                        StoreSection(
                            store = cartStores[index],
                            onQuantityChange = { itemId, newQuantity ->
                                // itemId here is the product ID for backend API
                                viewModel.updateQuantity(itemId, newQuantity)
                            },
                            onRemoveItem = { itemId ->
                                // itemId here is the product ID for backend API
                                viewModel.removeFromCart(itemId)
                            }
                        )
                    }
                }
                
                // Promo code section
                item {
                    PromoCodeSection(
                        promoCode = promoCode,
                        onPromoCodeChange = { promoCode = it },
                        onApplyPromo = {
                            // Apply promo code
                        }
                    )
                }
                
                // Order summary
                item {
                    OrderSummarySection(cartStores = cartStores)
                }
                
                // Add spacing at bottom
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreSection(
    store: CartStore,
    onQuantityChange: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
        shape = RoundedCornerShape(Rounded.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            // Store header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = store.storeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${store.estimatedTime} • ${if (store.deliveryFee == 0f) "Free delivery" else "$${store.deliveryFee} delivery"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                TextButton(onClick = {}) {
                    Text("Add more", style = MaterialTheme.typography.labelLarge)
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = Spacing.sm))
            
            // Items
            store.items.forEach { item ->
                SwipeableCartItem(
                    item = item,
                    onQuantityChange = { onQuantityChange(item.itemId, it) },
                    onRemove = { onRemoveItem(item.itemId) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableCartItem(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { state ->
            if (state == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
                true
            } else {
                false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(Rounded.lg)
                    )
                    .padding(horizontal = Spacing.lg),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.xs),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Item image
                AsyncImage(
                    model = item.image ?: R.drawable.ic_launcher_background,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(Rounded.md)),
                    contentScale = ContentScale.Crop
                )
                
                // Item details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$${"%.2f".format(item.discountedPrice)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$${"%.2f".format(item.originalPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    
                    Text(
                        text = "Expires: ${item.expiryDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EcoColors.Orange500
                    )
                }
                
                // Quantity selector
                QuantitySelector(
                    quantity = item.quantity,
                    onQuantityChange = onQuantityChange
                )
            }
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        IconButton(
            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.widthIn(min = 24.dp)
        )
        
        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromoCodeSection(
    promoCode: String,
    onPromoCodeChange: (String) -> Unit,
    onApplyPromo: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
        shape = RoundedCornerShape(Rounded.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalOffer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            TextField(
                value = promoCode,
                onValueChange = onPromoCodeChange,
                placeholder = { Text("Enter promo code") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
            
            TextButton(onClick = onApplyPromo) {
                Text("Apply", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun OrderSummarySection(cartStores: List<CartStore>) {
    val subtotal = cartStores.sumOf { store ->
        store.items.sumOf { it.discountedPrice.toDouble() * it.quantity }
    }.toFloat()
    val deliveryFees = cartStores.sumOf { it.deliveryFee.toDouble() }.toFloat()
    val discount = cartStores.sumOf { store ->
        store.items.sumOf { (it.originalPrice - it.discountedPrice).toDouble() * it.quantity }
    }.toFloat()
    val total = subtotal + deliveryFees
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
        shape = RoundedCornerShape(Rounded.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Divider()
            
            SummaryRow("Subtotal", subtotal)
            SummaryRow("Delivery Fees", deliveryFees)
            SummaryRow("Total Savings", -discount, color = EcoColors.Green600)
            
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${"%.2f".format(total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    amount: Float,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${if (amount < 0) "-" else ""}$${"%.2f".format(kotlin.math.abs(amount))}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun CartBottomBar(
    cartStores: List<CartStore>,
    onCheckout: () -> Unit
) {
    val total = cartStores.sumOf { store ->
        store.items.sumOf { it.discountedPrice.toDouble() * it.quantity } + store.deliveryFee
    }.toFloat()
    
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Elevation.lg
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.md)
                .padding(top = Spacing.md)
                .padding(bottom = 80.dp) // Extra padding for bottom navigation bar
        ) {
            // Checkout button - prominent at top
            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(Rounded.xl),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A) // EcoGreen
                )
            ) {
                Text(
                    text = "Proceed to Checkout",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = "$${"%.2f".format(total)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun EmptyCartView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        Text(
            text = "Your cart is empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "Start adding items to save food and money!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(Spacing.xl))
        
        Button(
            onClick = {},
            shape = RoundedCornerShape(Rounded.xl)
        ) {
            Text("Browse Stores")
        }
    }
}
