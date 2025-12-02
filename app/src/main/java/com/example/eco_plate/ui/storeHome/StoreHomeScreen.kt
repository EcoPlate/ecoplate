package com.example.eco_plate.ui.storeHome

import android.R.attr.label
import android.R.attr.text
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ChipColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eco_plate.ui.components.EcoColors
import com.example.eco_plate.ui.search.SearchProduct
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.chunked
import kotlin.collections.filter
import kotlin.text.format

data class StoreProduct(
    val id: String,
    val name: String,
    val price: Float,
    val originalPrice: Float? = null,
    val discount: Int? = null,
    val imageUrl: String,
    val category: String,
    val isEcoFriendly: Boolean = false,
    val expiryDate: String? = null,
    val quantity: Int = 0
)

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Float,
    val originalPrice: Float? = null,
    val discount: Int? = null,
    val quantity: String,
    val imageUrl: String,
    val isEdited: Boolean = false,
    val expiryDate: String = "",
    val description: String = "",
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

data class SearchFilter(
    val priceRange: ClosedFloatingPointRange<Float> = 0f..100f,
    val categories: List<String> = emptyList(),
    val stores: List<String> = emptyList(),
    val isOrganic: Boolean = false,
    val isEcoFriendly: Boolean = false,
    val sortBy: SortOption = SortOption.RELEVANCE
)
enum class SortOption {
    RELEVANCE, PRICE_LOW_HIGH, PRICE_HIGH_LOW, RATING, DISCOUNT
}
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
fun StoreHomeScreen (
    storeId: String,
    viewModel: StoreHomeViewModel,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit = {},
    onAddNewItem: () -> Unit = {},
    showBackButton: Boolean = false // Store owners don't need back button on their home
) {
    // Observe store data from ViewModel
    val storeItems by viewModel.storeItems.observeAsState()
    val currentStore by viewModel.currentStore.observeAsState()
    
    var selectedCategory by remember { mutableStateOf<String?>("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(SearchFilter()) }
    var isSearching by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Get categories from actual items
    val categories = remember(storeItems) {
        val itemCategories = storeItems?.data?.mapNotNull { it.category?.toString() }?.distinct() ?: emptyList()
        listOf("All") + itemCategories
    }

    // Convert Item model to Product for UI
    val allProducts = remember(storeItems) {
        storeItems?.data?.map { item ->
            Product(
                id = item.id,
                name = item.name,
                category = item.category.toString(),
                price = item.discountedPrice.toFloat(),
                originalPrice = if ((item.originalPrice ?: 0.0) > item.discountedPrice) {
                    item.originalPrice?.toFloat()
                } else null,
                discount = if (item.discountPercentage > 0) item.discountPercentage.toInt() else null,
                quantity = item.quantity.toString(),
                imageUrl = item.imageUrl ?: "https://images.unsplash.com/photo-1542838132-92c53300491e?w=400",
                isEdited = false,
                expiryDate = item.expiryDate ?: "",
                description = item.description ?: "",
                isEcoFriendly = item.tags?.any { it.contains("organic", ignoreCase = true) || it.contains("eco", ignoreCase = true) } == true
            )
        } ?: emptyList()
    }
    
    var searchResults by remember(allProducts) { mutableStateOf(allProducts) }
    
    // Load store data on first launch
    LaunchedEffect(storeId) {
        viewModel.loadStoreData(storeId)
        viewModel.loadStoreItems(storeId)
    }
    
    // Refresh when coming back to this screen
    LaunchedEffect(Unit) {
        viewModel.refreshStore()
    }


    var showQuickEditSheet by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    //Search effect
    LaunchedEffect(searchQuery, selectedCategory, filters) {
        if (searchQuery.isNotEmpty() || selectedCategory != null) {
            isSearching = true
            delay(300) // Debounce
            searchResults = filterProducts(allProducts, searchQuery, selectedCategory, filters)
            isSearching = false
        } else {
            searchResults = allProducts
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentStore?.data?.name ?: "My Store",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${allProducts.size} products",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onAddNewItem) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Product")
                    }
                    IconButton(onClick = { viewModel.refreshStore() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
            item {
                //Search Bar
                SearchTopBar(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onBackClick = onBackClick,
                    onClearSearch = { searchQuery = "" },
                    onFilterClick = { showFilters = true },
                    //cartItemsCount = cartItemsCount,
                    //onCartClick = onNavigateToCart
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
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }
            // Show loading state
            if (storeItems is com.example.eco_plate.utils.Resource.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = EcoColors.Green600)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading your products...")
                        }
                    }
                }
            }
            // Show empty state
            else if (allProducts.isEmpty() && searchQuery.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No products yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Add your first product to start selling",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onAddNewItem,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EcoColors.Green600
                            )
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add First Product")
                        }
                    }
                }
            }
            // Show search results or all products
            else {
                // Search Results
                if (isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                else if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        NoResultsFound(searchQuery)
                    }
                }
                else {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${searchResults.size} products",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = when (filters.sortBy) {
                                        SortOption.RELEVANCE ->  "Relevance"
                                        SortOption.PRICE_LOW_HIGH->  "Price: Low to High"
                                        SortOption.PRICE_HIGH_LOW -> "Price: High to Low"
                                        SortOption.RATING -> "Rating"
                                        SortOption.DISCOUNT  -> "Discount"
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            // Results Grid - responsive layout from search
            items(searchResults.chunked(2)) { rowProducts ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowProducts.forEach { product ->
                        StoreProductCard(
                            product = product,
                            //onClick = { onProductClick(product.id) },
//                            onAddToCart = {
//                                //viewModel.addToCart(product)
//                                coroutineScope.launch {
//                                    snackbarHostState.showSnackbar(
//                                        message = "${product.name} added to cart",
//                                        duration = SnackbarDuration.Short
//                                    )
//                                }
//                            },
                            onEditClick = {
                                productToEdit = product
                                showQuickEditSheet = true
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
        // If need to edit
        if (showQuickEditSheet && productToEdit != null) {
            QuickEditBottomSheet(
                product = productToEdit!!,
                onDismiss = { showQuickEditSheet = false },
                onProductUpdate = { updatedProduct ->
                    Log.d("QuickEdit", "Saving updated product: $updatedProduct")
                    
                    // Calculate new price based on discount percentage
                    val discountValue = updatedProduct.discount ?: 0
                    val originalPriceValue: Double = updatedProduct.originalPrice?.toDouble() ?: updatedProduct.price.toDouble()
                    val newPrice: Double = if (discountValue > 0) {
                        originalPriceValue * (1.0 - discountValue.toDouble() / 100.0)
                    } else {
                        originalPriceValue
                    }
                    
                    // Convert date to ISO-8601 format (Prisma expects full datetime)
                    val isoExpiryDate = updatedProduct.expiryDate.takeIf { it.isNotBlank() }?.let { date ->
                        // If it's already ISO format, use as-is; otherwise append time component
                        if (date.contains("T")) date else "${date}T23:59:59.000Z"
                    }
                    
                    // Call viewModel to save to database
                    // Backend calculates discount from currentPrice and originalPrice
                    viewModel.updateItem(
                        itemId = updatedProduct.id,
                        stockQuantity = updatedProduct.quantity.toIntOrNull(),
                        expiryDate = isoExpiryDate,
                        bestBefore = isoExpiryDate,
                        currentPrice = newPrice,
                        originalPrice = originalPriceValue,
                        isClearance = discountValue > 20
                    )
                    
                    // Show confirmation
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "${updatedProduct.name} updated successfully",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }

    }
}

@Composable
private fun StoreProductCard(
    product: Product,
    //onAddToCart: () -> Unit,
    onEditClick: () -> Unit, // Changed from onAddToCart
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
//                IconButton(
//                    onClick = onAddToCart,
//                    modifier = Modifier
//                        .align(Alignment.BottomEnd)
//                        .padding(4.dp)
//                        .size(32.dp)
//                        .background(
//                            MaterialTheme.colorScheme.primary,
//                            CircleShape
//                        )
//                ) {
//                    Icon(
//                        Icons.Filled.Add,
//                        contentDescription = "Add to Cart",
//                        tint = Color.White,
//                        modifier = Modifier.size(18.dp)
//                    )
//                }
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(
                            EcoColors.Green600,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit product",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearSearch: () -> Unit,
    onFilterClick: () -> Unit,
    //cartItemsCount: Int = 0,
    //onCartClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search for products...") },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            IconButton(onClick = onFilterClick) {
                Icon(Icons.Outlined.FilterList, "Filters")
            }
        }
    }
}

@Composable
private fun NoResultsFound(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Outlined.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "No results found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "We couldn't find any products matching \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        OutlinedButton(
            onClick = { /* Clear filters */ }
        ) {
            Text("Clear Filters")
        }
    }
}

@Composable
private fun SearchSuggestionSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    //chipColors: .default
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Use LazyRow for horizontal scrolling of chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions.size) { index ->
                SuggestionChip(
                    onClick = { onSuggestionClick(suggestions[index]) },
                    label = {
                        Text(
                            suggestions[index],
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    },
//                    colors = if (chipColors == com.example.eco_plate.ui.storeHome.ChipColors.trending) {
//                        SuggestionChipDefaults.suggestionChipColors(
//                            containerColor = EcoColors.Green100,
//                            labelColor = EcoColors.Green600
//                        )
//                    } else {
//                        SuggestionChipDefaults.suggestionChipColors()
//                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickEditBottomSheet(
    product: Product,
    onProductUpdate: (Product) -> Unit,
    //onNavigateToFullEdit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // A local, mutable copy to track edits before saving
    var editableProduct by remember { mutableStateOf(product) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp), // Extra padding for buttons
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER ---
            Text(
                text = "Edit and Add: ${product.name}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // --- QUANTITY FIELD ---
            Column {
                Text(
                    text = "Quantity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
                QuantityStepper(
                    quantity = editableProduct.quantity.toIntOrNull() ?: 0,
                    onQuantityChange = { newQuantity ->
                        editableProduct = editableProduct.copy(quantity = newQuantity.toString())
                    }
                )
            }

            // --- EXPIRY DATE FIELD ---
            Box {
                OutlinedTextField(
                    value = editableProduct.expiryDate ?: "",
                    onValueChange = { /* No-op, read-only */ },
                    label = { Text("Expiry Date") },
                    placeholder = { Text("Select a date") },
                    readOnly = true, // Makes the field not directly editable
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                // Add a clickable surface on top to trigger the dialog
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }


            // --- DISCOUNTED PRICE FIELD ---

            var discountText by remember { mutableStateOf(product.discount?.toString() ?: "") }
            OutlinedTextField(
                value = discountText,
                onValueChange = { newText ->
                    if (newText.all { it.isDigit() }) {
                        val percent = newText.toIntOrNull() ?: 0
                        if (newText.isEmpty() || percent in 0..100) {
                            discountText = newText
                            editableProduct = editableProduct.copy(discount = newText.toIntOrNull() ?: 0)
                        }
                    }
                },
                label = { Text("Edit Discount Percentage") },
                suffix = { Text("%") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            // --- ACTION BUTTONS ---
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // "More Options" button
                Button(
//                   onClick = { onNavigateToFullEdit(product.id) },
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                // "Save" button
                Button(
                    onClick = {
                        onProductUpdate(editableProduct) // Pass updated product back
                        onDismiss() // Close the sheet
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
        //when date picker called
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                            // Convert the selected date (in millis) to a formatted String
                            val selectedDateMillis = datePickerState.selectedDateMillis
                            if (selectedDateMillis != null) {
                                val instant = Instant.ofEpochMilli(selectedDateMillis)
                                // We add one day because the picker often returns the previous day in UTC
                                val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"))
                                val formattedDate = zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE) // "YYYY-MM-DD"
                                editableProduct = editableProduct.copy(expiryDate = formattedDate)
                            }
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Standard height for text fields
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.extraSmall
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minus Button
        IconButton(
            onClick = {
                // Ensure quantity doesn't go below 0
                onQuantityChange((quantity - 1).coerceAtLeast(0))
            },
            modifier = Modifier.size(56.dp) // Square button
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease quantity"
            )
        }

        // Divider
        VerticalDivider(
            modifier = Modifier.height(32.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )

        // Editable Quantity TextField in the middle
        BasicTextField(
            value = quantity.toString(),
            onValueChange = { text ->
                // Allow empty string for temporary state, default to 0 if parsing fails
                val newQuantity = if (text.isEmpty()) {
                    0 // Treat an empty field as a quantity of 0
                } else {
                    text.toIntOrNull() ?: quantity // If input is invalid, revert to last valid quantity
                }
                onQuantityChange(newQuantity.coerceAtLeast(0))
            },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface // Ensure text color is correct
            ),
            decorationBox = { innerTextField ->
                // This centers the text vertically
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                }
            }
        )

        // Divider
        VerticalDivider(
            modifier = Modifier.height(32.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )

        // Plus Button
        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            modifier = Modifier.size(56.dp) // Square button
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase quantity"
            )
        }
    }
}


private enum class ChipColors {
    default, trending
}

//Helper


private fun filterProducts(
    products: List<Product>,
    query: String,
    category: String?,
    filters: SearchFilter
): List<Product> {
    return products
        .filter { product ->
            (query.isEmpty() || product.name.contains(query, ignoreCase = true) )
        }
        .filter { product ->
            category == null || product.category == category
        }
        .filter { product ->
            product.price in filters.priceRange
        }
        .sortedBy { product ->
            when (filters.sortBy) {
                SortOption.RELEVANCE -> 0
                SortOption.PRICE_LOW_HIGH -> product.price.toInt()
                SortOption.PRICE_HIGH_LOW -> -product.price.toInt()
                SortOption.RATING -> 0
                SortOption.DISCOUNT -> -(product.discount ?: 0)
            }
        }
}