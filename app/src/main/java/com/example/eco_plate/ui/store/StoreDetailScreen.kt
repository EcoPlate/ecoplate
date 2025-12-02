@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.eco_plate.ui.store

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.ui.components.EcoColors
import com.example.eco_plate.ui.home.ProductItem
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.launch

data class StoreProduct(
    val id: String,
    val name: String,
    val price: Float,
    val originalPrice: Float? = null,
    val discount: Int? = null,
    val imageUrl: String,
    val category: String,
    val isEcoFriendly: Boolean = false
)

// Helper to convert API Item to StoreProduct
private fun Item.toStoreProduct(): StoreProduct {
    val discountPercent = if (originalPrice != null && originalPrice > currentPrice) {
        ((originalPrice - currentPrice) / originalPrice * 100).toInt()
    } else null
    
    return StoreProduct(
        id = id,
        name = name,
        price = currentPrice.toFloat(),
        originalPrice = originalPrice?.toFloat(),
        discount = discountPercent,
        imageUrl = imageUrl ?: "https://images.unsplash.com/photo-1534723452862-4c874018d66d?w=400",
        category = category ?: "Other",
        isEcoFriendly = isClearance == true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    storeId: String,
    viewModel: StoreDetailViewModel,
    onBackClick: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val cartItemsCount by viewModel.cartItemsCount.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Search state
    var searchQuery by remember { mutableStateOf("") }
    val isSearching by viewModel.isSearching.observeAsState(false)
    val isSyncing by viewModel.isSyncing.observeAsState(false)
    val filteredItemsFromSearch by viewModel.filteredItems.observeAsState(emptyList())
    
    // Fetch store items when screen loads
    LaunchedEffect(storeId) {
        viewModel.loadStoreItems(storeId)
        viewModel.loadStoreDetails(storeId)
    }
    
    val storeItemsResource by viewModel.storeItems.observeAsState()
    val storeDetailsResource by viewModel.storeDetails.observeAsState()
    
    val isLoading = storeItemsResource is Resource.Loading
    
    // Convert API items to StoreProducts - use filtered items if searching
    val products = if (searchQuery.isNotBlank()) {
        filteredItemsFromSearch.map { it.toStoreProduct() }
    } else {
        when (val resource = storeItemsResource) {
            is Resource.Success -> resource.data?.map { it.toStoreProduct() } ?: emptyList()
            else -> emptyList()
        }
    }
    
    // Check if sync is in progress (from API response message)
    val syncInProgress = storeItemsResource is Resource.Success && products.isEmpty() && searchQuery.isBlank()
    
    // State for selected product detail
    var selectedProduct by remember { mutableStateOf<StoreProduct?>(null) }
    
    // Get store info from details or items
    val storeName = when (val resource = storeDetailsResource) {
        is Resource.Success -> resource.data?.name ?: "Store"
        else -> "Store"
    }
    
    val storeImageUrl = when (val resource = storeDetailsResource) {
        is Resource.Success -> resource.data?.imageUrl ?: resource.data?.logo ?: "https://images.unsplash.com/photo-1534723452862-4c874018d66d?w=800"
        else -> "https://images.unsplash.com/photo-1534723452862-4c874018d66d?w=800"
    }
    
    // Extract unique categories from products
    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct()
    }
    
    val filteredProducts = remember(selectedCategory, products) {
        if (selectedCategory == "All") {
            products
        } else {
            products.filter { it.category == selectedCategory }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cartItemsCount > 0) {
                                Badge {
                                    Text(text = cartItemsCount.toString())
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToCart) {
                            Icon(Icons.Outlined.ShoppingCart, "Cart")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Store Header Image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = storeImageUrl,
                        contentDescription = storeName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )
                }
            }
            
            // Store Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = storeName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "4.5",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${products.size} items",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        viewModel.searchInStore(query, forceSync = false)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search products in this store...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.clearSearch()
                            }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        } else if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            // Force sync when user presses Enter/Search on keyboard
                            if (searchQuery.isNotBlank()) {
                                viewModel.searchInStore(searchQuery, forceSync = true)
                            }
                        }
                    )
                )
            }
            
            // Category Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { 
                                selectedCategory = category
                                // Clear search when changing category
                                if (searchQuery.isNotBlank()) {
                                    searchQuery = ""
                                    viewModel.clearSearch()
                                }
                            },
                            label = { Text(category) }
                        )
                    }
                }
            }
            
            // Products Grid
            item {
                Text(
                    text = if (searchQuery.isNotBlank()) "Search Results" else "Available Products",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Loading state
            if (isLoading || isSyncing || isSearching) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            if (isSyncing && searchQuery.isNotBlank()) {
                                Text(
                                    text = "Searching grocery stores for \"$searchQuery\"...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else if (isSearching) {
                                Text(
                                    text = "Searching...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else if (filteredProducts.isEmpty()) {
                // Empty state - only show when not syncing or searching
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (syncInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Syncing products...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Products are being fetched for this store. This may take a moment.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No products found for \"$searchQuery\"" else "No products available",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) "Try a different search term" else "Check back later for new items",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Refresh button
                        Button(
                            onClick = { 
                                viewModel.loadStoreItems(storeId)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Refresh Products")
                        }
                    }
                }
            } else {
            // Products in Grid Layout
            items(filteredProducts.chunked(2)) { rowProducts ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowProducts.forEach { product ->
                        StoreProductCard(
                            product = product,
                            onAddToCart = {
                                viewModel.addToCart(
                                        storeId = storeId,
                                        storeName = storeName,
                                    productId = product.id,
                                    productName = product.name,
                                    price = product.price,
                                    imageUrl = product.imageUrl
                                )
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "${product.name} added to cart",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                                onClick = { selectedProduct = product },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Add spacer if odd number of items
                    if (rowProducts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
    
    // Product Detail Dialog
    selectedProduct?.let { product ->
        ProductDetailDialog(
            product = product,
            storeName = storeName,
            onDismiss = { selectedProduct = null },
            onAddToCart = {
                viewModel.addToCart(
                    storeId = storeId,
                    storeName = storeName,
                    productId = product.id,
                    productName = product.name,
                    price = product.price,
                    imageUrl = product.imageUrl
                )
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "${product.name} added to cart",
                        duration = SnackbarDuration.Short
                    )
                }
                selectedProduct = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailDialog(
    product: StoreProduct,
    storeName: String,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Product Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                    
                    // Discount badge
                    product.discount?.let { discount ->
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF5252)
                        ) {
                            Text(
                                text = "-$discount% OFF",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // Product Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Name
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Store & Category (stacked for long names)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Outlined.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = storeName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Category,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Eco-friendly badge
                    if (product.isEcoFriendly) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Eco,
                                contentDescription = null,
                                tint = EcoColors.Green500,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Eco-Friendly / Clearance Item",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EcoColors.Green500,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Divider()
                    
                    // Price Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Price",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$${String.format("%.2f", product.price)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (product.discount != null) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface
                                )
                                product.originalPrice?.let { originalPrice ->
                                    Text(
                                        text = "$${String.format("%.2f", originalPrice)}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                }
                            }
                        }
                        
                        product.discount?.let { discount ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFF5252).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "Save $${String.format("%.2f", (product.originalPrice ?: product.price) - product.price)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFFF5252),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Add to Cart Button
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add to Cart",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreProductCard(
    product: StoreProduct,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Discount badge
                product.discount?.let { discount ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF5252)
                    ) {
                        Text(
                            text = "-$discount%",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                // Eco badge
                if (product.isEcoFriendly) {
                    Icon(
                        imageVector = Icons.Outlined.Eco,
                        contentDescription = "Eco-Friendly",
                        tint = EcoColors.Green500,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
                
                // Add to Cart Button
                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add to Cart",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Product Info
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (product.discount != null) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface
                    )
                    product.originalPrice?.let { originalPrice ->
                        Text(
                            text = "$${String.format("%.2f", originalPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }
            }
        }
    }
}
