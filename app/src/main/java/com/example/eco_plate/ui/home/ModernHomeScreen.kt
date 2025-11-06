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
    
    val nearbyStores = remember {
        listOf(
            StoreInfo("1", "Whole Foods Market", 4.8f, "10-15 min", 
                "https://images.unsplash.com/photo-1534723452862-4c874018d66d?w=400"),
            StoreInfo("2", "Trader Joe's", 4.7f, "15-20 min", 
                "https://images.unsplash.com/photo-1542838132-92c53300491e?w=400"),
            StoreInfo("3", "Safeway", 4.5f, "5-10 min", 
                "https://images.unsplash.com/photo-1578916171728-46686eac8d58?w=400"),
            StoreInfo("4", "Save-On-Foods", 4.6f, "10-15 min", 
                "https://images.unsplash.com/photo-1604719312566-8912e9227c6a?w=400"),
            StoreInfo("5", "IGA Marketplace", 4.4f, "15-20 min", 
                "https://images.unsplash.com/photo-1588964895597-cfccd6e2dbf9?w=400"),
            StoreInfo("6", "T&T Supermarket", 4.9f, "20-25 min", 
                "https://images.unsplash.com/photo-1553531889-e6cf4d692b1b?w=400"),
            StoreInfo("7", "Fresh St. Market", 4.7f, "12-18 min", 
                "https://images.unsplash.com/photo-1583258292688-d0213dc5a3a8?w=400"),
            StoreInfo("8", "Urban Fare", 4.6f, "18-22 min", 
                "https://images.unsplash.com/photo-1601599561213-832382fd07ba?w=400"),
            StoreInfo("9", "Choices Markets", 4.8f, "8-12 min", 
                "https://images.unsplash.com/photo-1540713434306-58505cf1b6fc?w=400"),
            StoreInfo("10", "Nesters Market", 4.3f, "3-7 min", 
                "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=400")
        )
    }
    
    val popularProducts = remember {
        listOf(
            ProductItem("p1", "Organic Tomatoes", "Whole Foods", 4.99f, 2.49f, 50,
                "https://images.unsplash.com/photo-1546470427-227e2f27b271?w=400"),
            ProductItem("p2", "Fresh Lettuce", "Trader Joe's", 3.99f, 1.99f, 50,
                "https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=400"),
            ProductItem("p3", "Sweet Corn", "Safeway", 5.99f, 2.99f, 50,
                "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=400"),
            ProductItem("p4", "Red Apples", "Save-On-Foods", 6.99f, 3.49f, 50,
                "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=400"),
            ProductItem("p5", "Fresh Oranges", "IGA", 5.49f, 2.74f, 50,
                "https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=400"),
            ProductItem("p6", "Ripe Bananas", "T&T", 2.99f, 1.49f, 50,
                "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400"),
            ProductItem("p7", "Green Peppers", "Urban Fare", 4.49f, 2.24f, 50,
                "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=400"),
            ProductItem("p8", "Fresh Carrots", "Choices", 3.49f, 1.74f, 50,
                "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400")
        )
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
                        title = "Expiring Soon - Save Now!",
                        actionText = "View all"
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
