package com.example.eco_plate.ui.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.scale
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
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.eco_plate.ui.components.EcoColors
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
    val imageUrl: String? = null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchScreen(
    viewModel: SearchViewModel,
    onBackClick: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    onStoreClick: (String) -> Unit = {},
    onNavigateToCart: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(SearchFilter()) }
    var isSearching by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val cartItemsCount by viewModel.cartItemsCount.collectAsState()
    
    val categories = remember {
        listOf("All", "Vegetables", "Fruits", "Dairy", "Meat", "Bakery", "Snacks", "Beverages", "Frozen", "Organic")
    }
    
    val recentSearches = remember {
        listOf("Organic Avocados", "Whole Wheat Bread", "Almond Milk", "Fresh Strawberries")
    }
    
    val trendingSearches = remember {
        listOf("Zero Waste", "Plant-Based", "Local Produce", "Gluten Free", "Vegan Options")
    }
    
    // Sample products
    val allProducts = remember { generateSampleProducts() }
    var searchResults by remember { mutableStateOf(allProducts) }
    
    // Search effect
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
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            SearchTopBar(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onBackClick = onBackClick,
                onClearSearch = { searchQuery = "" },
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
                            category = category,
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
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(5) { index ->
                            PopularProductCard(
                                product = allProducts[index],
                                onClick = { onProductClick(allProducts[index].id) },
                                onAddToCart = {
                                    viewModel.addToCart(allProducts[index])
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "${allProducts[index].name} added to cart",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            )
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
                } else if (searchResults.isEmpty()) {
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
                                        viewModel.addToCart(product)
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
    val coroutineScope = rememberCoroutineScope()
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
                    var isAdded by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (isAdded) 1.2f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "button_scale"
                    )
                    
                    IconButton(
                        onClick = {
                            if (!isAdded) {
                                isAdded = true
                                onAddToCart()
                            // Reset after animation
                            coroutineScope.launch {
                                delay(1000)
                                isAdded = false
                            }
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .scale(scale)
                            .background(
                                if (isAdded) EcoColors.Green500 else MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    ) {
                        AnimatedContent(
                            targetState = isAdded,
                            transitionSpec = {
                                fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                            },
                            label = "icon_change"
                        ) { added ->
                            Icon(
                                imageVector = if (added) Icons.Filled.Check else Icons.Filled.Add,
                                contentDescription = if (added) "Added to Cart" else "Add to Cart",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
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
    val coroutineScope = rememberCoroutineScope()
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
                var isAdded by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isAdded) 1.2f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "button_scale"
                )
                
                IconButton(
                    onClick = {
                        if (!isAdded) {
                            isAdded = true
                            onAddToCart()
                            // Reset after animation
                            coroutineScope.launch {
                                delay(1000)
                                isAdded = false
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .scale(scale)
                        .background(
                            if (isAdded) EcoColors.Green500 else MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                ) {
                    AnimatedContent(
                        targetState = isAdded,
                        transitionSpec = {
                            fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                        },
                        label = "icon_change"
                    ) { added ->
                        Icon(
                            imageVector = if (added) Icons.Filled.Check else Icons.Filled.Add,
                            contentDescription = if (added) "Added to Cart" else "Add to Cart",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
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

// Helper functions
private fun generateSampleProducts(): List<SearchProduct> {
    return listOf(
        SearchProduct("1", "Organic Avocados", "Whole Foods", "Vegetables", 5.99f, 7.99f, 25, 4.8f, true, true,
            imageUrl = "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=400"),
        SearchProduct("2", "Fresh Strawberries", "Trader Joe's", "Fruits", 3.99f, null, null, 4.7f, false, true,
            imageUrl = "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400"),
        SearchProduct("3", "Whole Wheat Bread", "Safeway", "Bakery", 2.49f, 3.49f, 30, 4.5f, true, false,
            imageUrl = "https://images.unsplash.com/photo-1549931319-a545dcf3bc73?w=400"),
        SearchProduct("4", "Almond Milk", "Save-On-Foods", "Dairy", 4.49f, null, null, 4.6f, true, true,
            imageUrl = "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400"),
        SearchProduct("5", "Free Range Eggs", "IGA", "Dairy", 6.99f, 8.99f, 20, 4.9f, true, true,
            imageUrl = "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400"),
        SearchProduct("6", "Wild Salmon", "T&T", "Meat", 14.99f, null, null, 4.8f, true, false,
            imageUrl = "https://images.unsplash.com/photo-1574781330855-d0db8cc6a79c?w=400"),
        SearchProduct("7", "Greek Yogurt", "Urban Fare", "Dairy", 5.49f, 6.99f, 15, 4.7f, false, false,
            imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400"),
        SearchProduct("8", "Quinoa Salad", "Fresh St.", "Prepared", 8.99f, null, null, 4.5f, true, true,
            imageUrl = "https://images.unsplash.com/photo-1505253716362-afaea1d3d1af?w=400"),
        SearchProduct("9", "Organic Kale", "Choices", "Vegetables", 3.49f, 4.49f, 25, 4.6f, true, true,
            imageUrl = "https://images.unsplash.com/photo-1524179091875-bf99a9a6af57?w=400"),
        SearchProduct("10", "Sourdough Bread", "Nesters", "Bakery", 4.99f, null, null, 4.8f, false, false,
            imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400"),
        SearchProduct("11", "Organic Bananas", "Whole Foods", "Fruits", 2.99f, 3.99f, 25, 4.7f, true, true,
            imageUrl = "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400"),
        SearchProduct("12", "Fresh Broccoli", "Save-On-Foods", "Vegetables", 3.49f, null, null, 4.6f, true, false,
            imageUrl = "https://images.unsplash.com/photo-1584270354949-c26b0d5b4a0c?w=400"),
    )
}

private fun filterProducts(
    products: List<SearchProduct>,
    query: String,
    category: String?,
    filters: SearchFilter
): List<SearchProduct> {
    return products
        .filter { product ->
            (query.isEmpty() || product.name.contains(query, ignoreCase = true) || 
             product.store.contains(query, ignoreCase = true))
        }
        .filter { product ->
            category == null || product.category == category
        }
        .filter { product ->
            product.price in filters.priceRange
        }
        .filter { product ->
            (!filters.isOrganic || product.isOrganic) &&
            (!filters.isEcoFriendly || product.isEcoFriendly)
        }
        .sortedBy { product ->
            when (filters.sortBy) {
                SortOption.RELEVANCE -> 0
                SortOption.PRICE_LOW_HIGH -> product.price.toInt()
                SortOption.PRICE_HIGH_LOW -> -product.price.toInt()
                SortOption.RATING -> -product.rating.toInt()
                SortOption.DISCOUNT -> -(product.discount ?: 0)
            }
        }
}
