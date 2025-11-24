package com.example.eco_plate.ui.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.AsyncImage
import com.example.eco_plate.ui.components.EcoColors
import com.example.eco_plate.utils.Resource
import com.example.eco_plate.data.repository.CartRepository
import com.example.eco_plate.data.repository.CartItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SearchProduct(
    val id: String,
    val name: String,
    val store: String,
    val category: String,
    val price: Float,
    val originalPrice: Float? = null,
    val discount: Int? = null,
    val rating: Float,
    val isEcoFriendly: Boolean = false,
    val isOrganic: Boolean = false,
    val isLocal: Boolean = false,
    val imageUrl: String? = null
)

data class SearchFilter(
    val priceRange: ClosedFloatingPointRange<Float> = 0f..100f,
    val maxPrice: Double? = null,
    val minDiscount: Float? = null,
    val categories: List<String> = emptyList(),
    val stores: List<String> = emptyList(),
    val isOrganic: Boolean = false,
    val isEcoFriendly: Boolean = false,
    val vegetarian: Boolean = false,
    val vegan: Boolean = false,
    val glutenFree: Boolean = false,
    val sortBy: SortOption = SortOption.RELEVANCE
)

enum class SortOption {
    RELEVANCE, PRICE_LOW_HIGH, PRICE_HIGH_LOW, RATING, DISCOUNT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchScreen(
    viewModel: SearchViewModel,
    cartRepository: CartRepository = remember { CartRepository() },
    onBackClick: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    onStoreClick: (String) -> Unit = {},
    onNavigateToCart: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchTrigger by remember { mutableStateOf("") } // Actual query to search
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(SearchFilter()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) } // Track if user has searched
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val cartItemsCount by cartRepository.cartItemsCount.collectAsState()
    
    val categories = remember {
        listOf("All", "VEGETABLES", "FRUITS", "DAIRY", "MEAT", "BAKERY", "SNACKS", "BEVERAGES", "FROZEN", "OTHER")
    }
    
    val recentSearches = remember {
        listOf("Organic Avocados", "Whole Wheat Bread", "Almond Milk", "Fresh Strawberries")
    }
    
    val trendingSearches = remember {
        listOf("Zero Waste", "Plant-Based", "Local Produce", "Gluten Free", "Vegan Options")
    }
    
    // Observe search results from ViewModel
    val itemSearchResults by viewModel.itemSearchResults.observeAsState()
    val nearbyStores by viewModel.nearbyStores.observeAsState()
    
    // Convert API results to SearchProduct format
    val searchResults = remember(itemSearchResults) {
        val results = itemSearchResults
        when (results) {
            is Resource.Success -> {
                results.data?.data?.map { item ->
                    SearchProduct(
                        id = item.id,
                        name = item.name,
                        store = item.storeName ?: item.store?.name ?: "Unknown Store",
                        category = item.category ?: "Other",
                        price = item.currentPrice.toFloat(),
                        originalPrice = if (item.originalPrice != null && item.originalPrice > item.currentPrice) {
                            item.originalPrice.toFloat()
                        } else null,
                        discount = item.discountPercent?.toInt() ?: 0,
                        rating = 4.5f, // Default rating until we add it to the model
                        isOrganic = item.tags?.contains("organic") == true,
                        isLocal = item.tags?.contains("local") == true,
                        imageUrl = item.images?.firstOrNull() ?: ""
                    )
                } ?: emptyList()
            }
            else -> emptyList()
        }
    }
    
    // Load nearby stores on screen launch
    LaunchedEffect(Unit) {
        viewModel.getNearbyStores()
        // Also load initial items
        viewModel.searchItems(query = null)
    }
    
    // Search effect with improved debouncing
    LaunchedEffect(searchQuery, selectedCategory, filters) {
        if (searchQuery.isNotEmpty() || selectedCategory != null || filters != SearchFilter()) {
            // Wait longer for user to finish typing
            delay(800) // Increased debounce time
            
            // Only search if query hasn't changed during the delay
            if (searchQuery == searchTrigger && searchQuery.isNotEmpty()) {
                return@LaunchedEffect
            }
            
            searchTrigger = searchQuery
            if (searchQuery.isNotEmpty() || selectedCategory != null || filters != SearchFilter()) {
                isSearching = true
                hasSearched = true
                
                viewModel.searchItems(
                    query = if (searchQuery.isNotEmpty()) searchQuery else null,
                    category = if (selectedCategory != null && selectedCategory != "All") selectedCategory else null,
                    nearBestBefore = if (filters.vegetarian) true else null,
                    isClearance = if (filters.vegan) true else null,
                    storeType = null,
                    maxPrice = filters.maxPrice,
                    minDiscount = filters.minDiscount?.toInt()
                )
                isSearching = false
            }
        } else if (searchQuery.isEmpty() && hasSearched) {
            // Clear results when search is cleared
            searchTrigger = ""
            hasSearched = false
            viewModel.clearSearchResults()
        }
    }
    
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            SearchTopBar(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onSearchSubmit = { 
                    // Manual submit on Enter key
                    searchTrigger = searchQuery
                    isSearching = true
                    hasSearched = true
                    
                    coroutineScope.launch {
                        viewModel.searchItems(
                            query = if (searchQuery.isNotEmpty()) searchQuery else null,
                            category = if (selectedCategory != null && selectedCategory != "All") selectedCategory else null,
                            nearBestBefore = if (filters.vegetarian) true else null,
                            isClearance = if (filters.vegan) true else null,
                            storeType = null,
                            maxPrice = filters.maxPrice,
                            minDiscount = filters.minDiscount?.toInt()
                        )
                        isSearching = false
                    }
                },
                onBackClick = onBackClick,
                onClearSearch = { 
                    searchQuery = ""
                    searchTrigger = ""
                    hasSearched = false
                    viewModel.clearSearchResults()
                },
                onFilterClick = { showFilters = true },
                cartItemsCount = cartItemsCount,
                onCartClick = onNavigateToCart
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp), // Add padding for bottom navigation
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Categories
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(categories) { category ->
                        CategoryChip(
                            category = category.lowercase().capitalize(),
                            isSelected = selectedCategory == category || (selectedCategory == null && category == "All"),
                            onClick = {
                                selectedCategory = if (category == "All") null else category
                            }
                        )
                    }
                }
            }
            
            // Recent/Trending Searches (show when no search)
            if (searchQuery.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Recent Searches
                        if (recentSearches.isNotEmpty()) {
                            SearchSuggestionSection(
                                title = "Recent Searches",
                                icon = Icons.Outlined.History,
                                suggestions = recentSearches,
                                onSuggestionClick = { searchQuery = it }
                            )
                        }
                        
                        // Trending Searches
                        SearchSuggestionSection(
                            title = "Trending Now",
                            icon = Icons.Outlined.TrendingUp,
                            suggestions = trendingSearches,
                            onSuggestionClick = { searchQuery = it },
                            chipColors = ChipColors.trending
                        )
                    }
                }
                
                // Popular Products Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Popular Right Now",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = {}) {
                            Text("View All")
                        }
                    }
                }
                
                // Popular Products Horizontal List
                if (searchResults.isNotEmpty()) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(minOf(5, searchResults.size)) { index ->
                                PopularProductCard(
                                    product = searchResults[index],
                                    onClick = { onProductClick(searchResults[index].id) },
                                    onAddToCart = {
                                        val product = searchResults[index]
                                        cartRepository.addToCart(
                                            CartItem(
                                                id = product.id,
                                                name = product.name,
                                                storeName = product.store,
                                                price = product.price,
                                                originalPrice = product.originalPrice,
                                                quantity = 1,
                                                imageUrl = product.imageUrl,
                                                isEcoFriendly = product.isLocal || product.isOrganic
                                            )
                                        )
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "${searchResults[index].name} added to cart",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
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
                } else if (searchResults.isEmpty() && hasSearched && !isSearching) {
                    // Only show "No results" if user has searched and we're not currently searching
                    item {
                        NoResultsFound(searchQuery)
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${searchResults.size} results",
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
                                        SortOption.RELEVANCE -> "Relevance"
                                        SortOption.PRICE_LOW_HIGH -> "Price: Low to High"
                                        SortOption.PRICE_HIGH_LOW -> "Price: High to Low"
                                        SortOption.RATING -> "Rating"
                                        SortOption.DISCOUNT -> "Discount"
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    
                    // Results Grid - responsive layout
                    items(searchResults.chunked(2)) { rowProducts ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowProducts.forEach { product ->
                                SearchResultCard(
                                    product = product,
                                    onClick = { onProductClick(product.id) },
                                    onAddToCart = { 
                                        cartRepository.addToCart(
                                            CartItem(
                                                id = product.id,
                                                name = product.name,
                                                storeName = product.store,
                                                price = product.price,
                                                originalPrice = product.originalPrice,
                                                quantity = 1,
                                                imageUrl = product.imageUrl,
                                                isEcoFriendly = product.isLocal || product.isOrganic
                                            )
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
    }
    
    // Filters Bottom Sheet
    if (showFilters) {
        FilterBottomSheet(
            currentFilters = filters,
            onApplyFilters = { 
                filters = it
                showFilters = false
            },
            onDismiss = { showFilters = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearchSubmit: () -> Unit = {},
    onBackClick: () -> Unit,
    onClearSearch: () -> Unit,
    onFilterClick: () -> Unit,
    cartItemsCount: Int = 0,
    onCartClick: () -> Unit = {}
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
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, "Back")
            }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search for products...") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearchSubmit() }
                ),
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
private fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(category) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun SearchSuggestionSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    chipColors: ChipColors = ChipColors.default
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
                    colors = if (chipColors == ChipColors.trending) {
                        SuggestionChipDefaults.suggestionChipColors(
                            containerColor = EcoColors.Green100,
                            labelColor = EcoColors.Green600
                        )
                    } else {
                        SuggestionChipDefaults.suggestionChipColors()
                    }
                )
            }
        }
    }
}

@Composable
private fun PopularProductCard(
    product: SearchProduct,
    onClick: () -> Unit,
    onAddToCart: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Display actual product image
                product.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                if (product.discount != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF5252)
                    ) {
                        Text(
                            text = "${product.discount}% OFF",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = product.store,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFFFC107)
                    )
                    Text(
                        text = "${product.rating}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    if (product.isEcoFriendly) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Outlined.Eco,
                            contentDescription = "Eco-Friendly",
                            modifier = Modifier.size(14.dp),
                            tint = EcoColors.Green500
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$${product.price}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (product.originalPrice != null) {
                            Text(
                                text = "$${product.originalPrice}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }
                    }
                    
                    // Add to Cart Button
                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
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
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    product: SearchProduct,
    onClick: () -> Unit,
    onAddToCart: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Display actual product image
                product.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Badges
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (product.discount != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFF5252)
                        ) {
                            Text(
                                text = "-${product.discount}%",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (product.isOrganic) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EcoColors.Green500
                        ) {
                            Text(
                                text = "ORGANIC",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
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
            
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.store,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (product.isEcoFriendly) {
                        Icon(
                            Icons.Outlined.Eco,
                            contentDescription = "Eco-Friendly",
                            modifier = Modifier.size(14.dp),
                            tint = EcoColors.Green500
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
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
                            if (product.originalPrice != null) {
                                Text(
                                    text = "$${product.originalPrice}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            }
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFFFC107)
                        )
                        Text(
                            text = "${product.rating}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
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
            text = if (query.isNotEmpty()) {
                "We couldn't find any products matching \"$query\""
            } else {
                "Try searching for a product or browsing categories"
            },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    currentFilters: SearchFilter,
    onApplyFilters: (SearchFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var tempFilters by remember { mutableStateOf(currentFilters) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { tempFilters = SearchFilter() }) {
                    Text("Clear All")
                }
            }
            
            HorizontalDivider()
            
            // Sort By
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Sort By",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    SortOption.values().forEach { option ->
                        FilterChip(
                            selected = tempFilters.sortBy == option,
                            onClick = { tempFilters = tempFilters.copy(sortBy = option) },
                            label = {
                                Text(
                                    when (option) {
                                        SortOption.RELEVANCE -> "Relevance"
                                        SortOption.PRICE_LOW_HIGH -> "Price ↑"
                                        SortOption.PRICE_HIGH_LOW -> "Price ↓"
                                        SortOption.RATING -> "Rating"
                                        SortOption.DISCOUNT -> "Discount"
                                    }
                                )
                            }
                        )
                    }
                }
            }
            
            // Price Range
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Price Range",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                RangeSlider(
                    value = tempFilters.priceRange,
                    onValueChange = { tempFilters = tempFilters.copy(priceRange = it) },
                    valueRange = 0f..100f,
                    steps = 9
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$${tempFilters.priceRange.start.toInt()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$${tempFilters.priceRange.endInclusive.toInt()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Special Filters
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Special Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tempFilters.isOrganic,
                            onCheckedChange = { tempFilters = tempFilters.copy(isOrganic = it) }
                        )
                        Text("Organic")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tempFilters.isEcoFriendly,
                            onCheckedChange = { tempFilters = tempFilters.copy(isEcoFriendly = it) }
                        )
                        Text("Eco-Friendly")
                    }
                }
            }
            
            // Apply Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { onApplyFilters(tempFilters) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}

private enum class ChipColors {
    default, trending
}

// Helper functions - API data is now used instead of sample products
