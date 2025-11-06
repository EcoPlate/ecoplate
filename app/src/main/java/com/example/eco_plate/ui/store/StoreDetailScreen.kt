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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.eco_plate.ui.components.EcoColors
import com.example.eco_plate.ui.home.ProductItem
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

data class StoreData(
    val id: String,
    val name: String,
    val imageUrl: String,
    val rating: Float,
    val reviews: Int,
    val deliveryTime: String,
    val deliveryFee: String,
    val categories: List<String>,
    val products: List<StoreProduct>
)

// Store-specific product data
val storeProducts = mapOf(
    "1" to StoreData( // Whole Foods
        id = "1",
        name = "Whole Foods Market",
        imageUrl = "https://images.unsplash.com/photo-1534723452862-4c874018d66d?w=800",
        rating = 4.8f,
        reviews = 324,
        deliveryTime = "10-15 min",
        deliveryFee = "Free",
        categories = listOf("Organic", "Fresh Produce", "Bakery", "Dairy"),
        products = listOf(
            StoreProduct("wf1", "Organic Avocados", 5.99f, 7.99f, 25, 
                "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=400", "Produce", true),
            StoreProduct("wf2", "Wild Salmon Fillet", 14.99f, 19.99f, 25,
                "https://images.unsplash.com/photo-1574781330855-d0db8cc6a79c?w=400", "Seafood"),
            StoreProduct("wf3", "Organic Kale", 3.49f, 4.99f, 30,
                "https://images.unsplash.com/photo-1524179091875-bf99a9a6af57?w=400", "Produce", true),
            StoreProduct("wf4", "Sourdough Bread", 4.99f, null, null,
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400", "Bakery"),
            StoreProduct("wf5", "Greek Yogurt", 5.49f, 6.99f, 20,
                "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400", "Dairy"),
            StoreProduct("wf6", "Organic Strawberries", 6.99f, 8.99f, 22,
                "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400", "Produce", true)
        )
    ),
    "2" to StoreData( // Trader Joe's
        id = "2",
        name = "Trader Joe's",
        imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800",
        rating = 4.7f,
        reviews = 412,
        deliveryTime = "15-20 min",
        deliveryFee = "$2.99",
        categories = listOf("Unique Finds", "International", "Snacks", "Wine"),
        products = listOf(
            StoreProduct("tj1", "Everything Bagel Seasoning", 2.99f, null, null,
                "https://images.unsplash.com/photo-1599599810694-b5b37304c041?w=400", "Seasonings"),
            StoreProduct("tj2", "Cauliflower Gnocchi", 3.99f, 4.99f, 20,
                "https://images.unsplash.com/photo-1609501676725-7186f017a4b7?w=400", "Frozen"),
            StoreProduct("tj3", "Cookie Butter", 3.49f, null, null,
                "https://images.unsplash.com/photo-1560683103-d24b57f4a022?w=400", "Spreads"),
            StoreProduct("tj4", "Mandarin Chicken", 4.99f, 5.99f, 17,
                "https://images.unsplash.com/photo-1569058242253-92a9c755a0ec?w=400", "Frozen"),
            StoreProduct("tj5", "Dark Chocolate Peanut Butter Cups", 4.49f, null, null,
                "https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=400", "Snacks")
        )
    ),
    "3" to StoreData( // Safeway
        id = "3",
        name = "Safeway",
        imageUrl = "https://images.unsplash.com/photo-1578916171728-46686eac8d58?w=800",
        rating = 4.5f,
        reviews = 298,
        deliveryTime = "5-10 min",
        deliveryFee = "Free on $35+",
        categories = listOf("Grocery", "Pharmacy", "Deli", "Bakery"),
        products = listOf(
            StoreProduct("sf1", "Fresh Ground Beef", 8.99f, 10.99f, 18,
                "https://images.unsplash.com/photo-1603048297172-c92544798d5b?w=400", "Meat"),
            StoreProduct("sf2", "White Bread", 2.49f, 2.99f, 17,
                "https://images.unsplash.com/photo-1549931319-a545dcf3bc73?w=400", "Bakery"),
            StoreProduct("sf3", "2% Milk Gallon", 4.49f, null, null,
                "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400", "Dairy"),
            StoreProduct("sf4", "Rotisserie Chicken", 7.99f, 9.99f, 20,
                "https://images.unsplash.com/photo-1598103442097-8b74394b95c6?w=400", "Deli"),
            StoreProduct("sf5", "Fresh Bananas", 2.99f, 3.49f, 14,
                "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400", "Produce")
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    storeId: String,
    viewModel: StoreDetailViewModel,
    onBackClick: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val store = remember { storeProducts[storeId] ?: storeProducts["1"]!! }
    var selectedCategory by remember { mutableStateOf("All") }
    val cartItemsCount by viewModel.cartItemsCount.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val categories = listOf("All") + store.categories
    
    val filteredProducts = remember(selectedCategory) {
        if (selectedCategory == "All") {
            store.products
        } else {
            store.products.filter { it.category == selectedCategory }
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
                        model = store.imageUrl,
                        contentDescription = store.name,
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
                        text = store.name,
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
                                text = "${store.rating} (${store.reviews})",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = store.deliveryTime,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = store.deliveryFee,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
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
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }
            
            // Products Grid
            item {
                Text(
                    text = "Available Products",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
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
                                    storeId = store.id,
                                    storeName = store.name,
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

@Composable
private fun StoreProductCard(
    product: StoreProduct,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${product.price}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (product.discount != null) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface
                    )
                    product.originalPrice?.let { originalPrice ->
                        Text(
                            text = "$${originalPrice}",
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
