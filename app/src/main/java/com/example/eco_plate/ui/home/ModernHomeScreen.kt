package com.example.eco_plate.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.eco_plate.R
import com.example.eco_plate.ui.components.*
import com.example.eco_plate.ui.location.LocationManager
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class CategoryItem(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Color
)

data class DealItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val discount: String,
    val emoji: String,
    val backgroundColor: Color
)

data class StoreInfo(
    val id: String,
    val name: String,
    val rating: Float,
    val deliveryTime: String,
    val imageUrl: String
)

data class ProductItem(
    val id: String,
    val name: String,
    val store: String,
    val originalPrice: Float,
    val discountedPrice: Float,
    val discount: Int,
    val imageUrl: String,
    val expiryDate: String = "Today"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernHomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToStore: (String) -> Unit = {},
    onNavigateToCategory: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    
    val categories = remember {
        listOf(
            CategoryItem("1", "Fruits", "🍎", EcoColors.Red500),
            CategoryItem("2", "Bread", "🍞", EcoColors.Orange500),
            CategoryItem("3", "Dairy", "🥛", EcoColors.Blue500),
            CategoryItem("4", "Meat", "🥩", Color(0xFFE91E63)),
            CategoryItem("5", "Sweets", "🍰", EcoColors.Purple500),
            CategoryItem("6", "Veggies", "🥬", EcoColors.Green500)
        )
    }
    
    val deals = remember {
        listOf(
            DealItem("1", "Fresh Produce", "Save up to", "50% OFF", "🥬", EcoColors.Green100),
            DealItem("2", "Bakery Items", "Today only", "30% OFF", "🥖", EcoColors.Orange500.copy(0.2f)),
            DealItem("3", "Dairy Special", "Limited time", "Buy 1 Get 1", "🧀", EcoColors.Blue500.copy(0.2f))
        )
    }
    
    // Get stores and products from viewModel
    val nearbyStoresResource by viewModel.nearbyStores.observeAsState()
    val featuredItemsResource by viewModel.featuredItems.observeAsState()
    val locationManager = LocationManager(LocalContext.current)
    
    // Load stores from backend when screen opens
    LaunchedEffect(Unit) {
        locationManager.getLastKnownLocation { location ->
            if (location != null) {
                viewModel.loadNearbyStores(location.latitude, location.longitude)
                viewModel.searchItems(location.latitude, location.longitude)
            } else {
                // Use default Vancouver location if no location available
                viewModel.loadNearbyStores(49.2827, -123.1207)
                viewModel.searchItems(49.2827, -123.1207)
            }
        }
    }
    
    // Convert backend stores to UI model
    val nearbyStores = when (val resource = nearbyStoresResource) {
        is Resource.Success -> {
            resource.data?.map { store ->
                StoreInfo(
                    id = store.id,
                    name = store.name,
                    rating = store.rating?.toFloat() ?: 4.5f,
                    deliveryTime = "${(10..30).random()} min",
                    imageUrl = store.imageUrl ?: "https://images.unsplash.com/photo-1534723452862-4c874018d66d?w=400"
                )
            } ?: emptyList()
        }
        else -> emptyList()
    }
    
    // Get all products from backend (not just discounted ones)
    val popularProducts = when (val resource = featuredItemsResource) {
        is Resource.Success -> {
            resource.data?.map { item ->
                ProductItem(
                    id = item.id,
                    name = item.name,
                    store = item.storeName ?: item.store?.name ?: "Store",
                    originalPrice = item.originalPrice?.toFloat() ?: item.currentPrice.toFloat(),
                    discountedPrice = item.currentPrice.toFloat(),
                    discount = if (item.originalPrice != null && item.originalPrice > item.currentPrice) {
                        ((1 - (item.currentPrice / item.originalPrice)) * 100).toInt()
                    } else 0,
                    imageUrl = item.images?.firstOrNull() ?: "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400",
                    expiryDate = item.expiryDate ?: item.bestBefore ?: "Fresh"
                )
            } ?: emptyList()
        }
        is Resource.Loading -> {
            // Show placeholder items while loading
            listOf(
                ProductItem("loading1", "Loading...", "Store", 0f, 0f, 0,
                    "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400")
            )
        }
        is Resource.Error -> {
            // Show empty list on error
            emptyList()
        }
        null -> {
            // Initial state - show empty list
            emptyList()
        }
    }
    
    Scaffold(
        topBar = {
            HomeTopBar(
                onNotificationClick = onNavigateToNotifications
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Location and Search Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(Spacing.md)
                ) {
                    // Delivery Address
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Deliver to",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "123 Main Street, Vancouver",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Change address"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(Spacing.md))
                    
                    // Search Bar
                    ModernSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search for stores or items...",
                        onSearch = onNavigateToSearch,
                        enabled = false,
                        modifier = Modifier.clickable { onNavigateToSearch() }
                    )
                }
            }
            
            // Featured Deals Carousel
            item {
                Column {
                    SectionHeader(
                        title = "Today's Deals 🔥",
                        actionText = "View all"
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        contentPadding = PaddingValues(horizontal = Spacing.md)
                    ) {
                        items(deals) { deal ->
                            DealCard(deal = deal)
                        }
                    }
                }
            }
            
            // Categories Grid
            item {
                Column {
                    SectionHeader(
                        title = "Categories",
                        actionText = "See all"
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        contentPadding = PaddingValues(horizontal = Spacing.md)
                    ) {
                        items(categories) { category ->
                            CategoryCard(
                                category = category,
                                onClick = { onNavigateToCategory(category.id) }
                            )
                        }
                    }
                }
            }
            
            // Nearby Stores Section
            item {
                SectionHeader(
                    title = "Nearby Stores",
                    actionText = "View map",
                    onActionClick = { 
                        // Special case: pass "map" to navigate to map screen
                        onNavigateToStore("map") 
                    }
                )
            }
            
            items(nearbyStores) { store ->
                StoreCard(
                    storeName = store.name,
                    storeImage = store.imageUrl,
                    rating = store.rating,
                    deliveryTime = store.deliveryTime,
                    deliveryFee = if (store.rating > 4.6f) "Free" else "$2.99",
                    discount = if (store.rating > 4.7f) 20 else null,
                    categories = listOf("Grocery", "Organic", "Fresh Produce"),
                    onClick = { onNavigateToStore(store.id) },
                    modifier = Modifier.padding(horizontal = Spacing.md)
                )
            }
            
            // Popular Items Section
            item {
                Column {
                    SectionHeader(
                        title = if (popularProducts.isNotEmpty()) "Available Products Near You" else "No Products Available",
                        actionText = if (popularProducts.isNotEmpty()) "View all" else ""
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        contentPadding = PaddingValues(horizontal = Spacing.md)
                    ) {
                        items(popularProducts) { product ->
                            ItemCard(
                                itemName = product.name,
                                itemImage = product.imageUrl,
                                originalPrice = product.originalPrice,
                                discountedPrice = product.discountedPrice,
                                discount = product.discount,
                                expiryDate = product.expiryDate,
                                onAddToCart = {
                                    viewModel.addToCart(
                                        storeId = product.store.replace(" ", "_").lowercase(),
                                        storeName = product.store,
                                        productId = product.id,
                                        productName = product.name,
                                        price = product.discountedPrice,
                                        imageUrl = product.imageUrl
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onNotificationClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(Spacing.sm))
                Column {
                    Text(
                        text = "EcoPlate",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getGreeting(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            Badge(
                modifier = Modifier.padding(top = 8.dp, end = 8.dp)
            ) {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun DealCard(deal: DealItem) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(150.dp),
        shape = RoundedCornerShape(Rounded.xl),
        colors = CardDefaults.cardColors(containerColor = deal.backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = deal.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = deal.discount,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = deal.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = deal.emoji,
                    fontSize = 48.sp
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: CategoryItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = category.color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = category.emoji,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 32.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun getGreeting(): String {
    val hour = LocalDateTime.now().hour
    return when (hour) {
        in 0..11 -> "Good morning! ☀️"
        in 12..16 -> "Good afternoon! 🌤️"
        in 17..20 -> "Good evening! 🌅"
        else -> "Good night! 🌙"
    }
}
