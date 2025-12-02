package com.example.eco_plate.ui.newItem

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import coil.compose.AsyncImage
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.data.models.ItemCategory
import com.example.eco_plate.data.models.NutritionInfo
import com.example.eco_plate.ui.components.EcoColors
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewItemScreen(
    viewModel: NewItemViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val createItemState by viewModel.createItemState.observeAsState()
    val barcodeLookupState by viewModel.barcodeLookupState.observeAsState()
    val imageScanState by viewModel.imageScanState.observeAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showScanner by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var showNutritionScanner by remember { mutableStateOf(false) }
    var showDiscountSection by remember { mutableStateOf(false) }
    var isLookingUpBarcode by remember { mutableStateOf(false) }
    var isScanningImage by remember { mutableStateOf(false) }
    
    // Image handling
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedImagePath by remember { mutableStateOf<String?>(null) }
    
    // Gallery picker launcher - scan image after selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            capturedImagePath = null
            // Auto-scan the selected image for product info
            viewModel.scanProductImage(uri)
        }
    }
    
    // Handle item creation result
    LaunchedEffect(createItemState) {
        when (val state = createItemState) {
            is Resource.Success -> {
                val createdItem = state.data
                val itemName = createdItem?.name ?: "Item"
                Toast.makeText(context, "$itemName created successfully! 🎉", Toast.LENGTH_LONG).show()
                onNavigateBack()
            }
            is Resource.Error -> {
                val errorMessage = state.message ?: "Failed to create item"
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
            }
            is Resource.Loading -> {
                // Show nothing for loading, handled elsewhere
            }
            null -> {
                // Initial state, do nothing
            }
        }
    }

    var item by remember {
        mutableStateOf(
            Item(
                id = "",
                name = "",
                brand = null,
                storeId = "",
                category = "OTHER",
                subcategory = null,
                originalPrice = 0.0,
                currentPrice = 0.0,
                discountPercent = null,
                sku = null,
                barcode = null,
                stockQuantity = 1,
                unit = null,
                bestBefore = null,
                expiryDate = null,
                isClearance = false,
                images = null,
                description = null,
                tags = null,
                isAvailable = true,
                availableFrom = null,
                availableUntil = null,
                createdAt = "",
                updatedAt = "",
                upc = null,
                plu = null,
                soldByWeight = false,
                allergens = null,
                nutritionInfo = null,
                storeName = null,
                storeType = null,
                storeAddress = null,
                storeLatitude = null,
                storeLongitude = null,
                distanceMeters = null,
                store = null
            )
        )
    }
    
    // Handle barcode lookup result - populate form with product data
    LaunchedEffect(barcodeLookupState) {
        when (barcodeLookupState) {
            is Resource.Success -> {
                val productInfo = (barcodeLookupState as Resource.Success).data
                if (productInfo != null) {
                    // Populate item with barcode data
                    item = item.copy(
                        name = productInfo.name ?: item.name,
                        brand = productInfo.brand ?: item.brand,
                        description = productInfo.description ?: item.description,
                        category = productInfo.category ?: item.category,
                        nutritionInfo = productInfo.nutritionInfo ?: item.nutritionInfo,
                        allergens = productInfo.allergens ?: item.allergens,
                        tags = (item.tags.orEmpty() + productInfo.tags.orEmpty()).distinct().ifEmpty { null }
                    )
                    // Set image from barcode lookup
                    productInfo.imageUrl?.let { url ->
                        selectedImageUri = Uri.parse(url)
                        capturedImagePath = null
                    }
                    isLookingUpBarcode = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Product found: ${productInfo.name}",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                viewModel.clearBarcodeLookup()
            }
            is Resource.Error -> {
                isLookingUpBarcode = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = (barcodeLookupState as Resource.Error).message ?: "Product not found",
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.clearBarcodeLookup()
            }
            is Resource.Loading -> {
                isLookingUpBarcode = true
            }
            else -> {}
        }
    }
    
    // Handle image scan result
    LaunchedEffect(imageScanState) {
        when (imageScanState) {
            is Resource.Success -> {
                val scanResult = (imageScanState as Resource.Success).data
                if (scanResult != null) {
                    isScanningImage = false
                    
                    if (scanResult.barcode != null) {
                        // Barcode found - update UPC field (lookup will be triggered automatically)
                        item = item.copy(upc = scanResult.barcode)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Barcode detected: ${scanResult.barcode}",
                                duration = SnackbarDuration.Short
                            )
                        }
                    } else if (scanResult.productName != null || scanResult.brand != null) {
                        // Text extracted - populate name/brand
                        item = item.copy(
                            name = if (item.name.isBlank()) scanResult.productName ?: item.name else item.name,
                            brand = if (item.brand.isNullOrBlank()) scanResult.brand else item.brand
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Product info extracted from image",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
                viewModel.clearImageScan()
            }
            is Resource.Error -> {
                isScanningImage = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = (imageScanState as Resource.Error).message ?: "Could not scan image",
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.clearImageScan()
            }
            is Resource.Loading -> {
                isScanningImage = true
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0) // Remove default insets so we can handle them manually
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Modern Top App Bar with gradient - extends behind status bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                EcoColors.Green600,
                                EcoColors.Green500
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Add New Item",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "List a product for your store",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    
                    // Placeholder for symmetry
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }

            // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .navigationBarsPadding(), // Add padding for bottom navigation bar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                // Photo Section Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.PhotoCamera,
                                contentDescription = null,
                                tint = EcoColors.Green600,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Product Photo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        // Photo Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedImageUri != null || capturedImagePath != null)
                                        Color.Transparent
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                selectedImageUri != null -> {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Selected product image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                capturedImagePath != null -> {
                                    val bitmap = android.graphics.BitmapFactory.decodeFile(capturedImagePath)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Captured product image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                else -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.AddPhotoAlternate,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "Add a photo of your product",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "Barcode will be auto-detected",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            
                            // Scanning overlay
                            if (isScanningImage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 3.dp
                                        )
                                        Text(
                                            "Scanning for product info...",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Photo Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Take Photo Button
                            OutlinedButton(
                                onClick = { showCamera = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = EcoColors.Green600
                                ),
                                border = BorderStroke(1.dp, EcoColors.Green600),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Take Photo")
                            }
                            
                            // Choose from Gallery Button
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EcoColors.Green600
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gallery")
                            }
                        }
                    }
                }

                // Basic Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = EcoColors.Green600,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Basic Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Item Name
            OutlinedTextField(
                value = item.name,
                            onValueChange = { item = item.copy(name = it) },
                            label = { Text("Product Name *") },
                            placeholder = { Text("e.g., Organic Bananas") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EcoColors.Green600,
                                focusedLabelColor = EcoColors.Green600
                            ),
                            singleLine = true
                        )

                        // Brand and Category Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                OutlinedTextField(
                    value = item.brand.orEmpty(),
                                onValueChange = { item = item.copy(brand = it.ifBlank { null }) },
                    label = { Text("Brand") },
                                placeholder = { Text("Optional") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EcoColors.Green600,
                                    focusedLabelColor = EcoColors.Green600
                                ),
                                singleLine = true
                            )
                            
                CategoryDropdown(
                                selected = try { ItemCategory.valueOf(item.category ?: "OTHER") } catch (e: Exception) { ItemCategory.OTHER },
                                onSelected = { item = item.copy(category = it.name) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Description
            OutlinedTextField(
                value = item.description.orEmpty(),
                            onValueChange = { item = item.copy(description = it.ifBlank { null }) },
                label = { Text("Description") },
                            placeholder = { Text("Describe your product...") },
                modifier = Modifier
                    .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EcoColors.Green600,
                                focusedLabelColor = EcoColors.Green600
                            ),
                            maxLines = 4
                        )

                        // UPC/Barcode Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = item.upc.orEmpty(),
                                onValueChange = { item = item.copy(upc = it.ifBlank { null }) },
                                label = { Text("UPC / Barcode") },
                                placeholder = { Text("Scan or enter") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EcoColors.Green600,
                                    focusedLabelColor = EcoColors.Green600
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    if (isLookingUpBarcode) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                            color = EcoColors.Green600
                                        )
                                    } else {
                                        IconButton(onClick = { showScanner = true }) {
                                            Icon(
                                                Icons.Filled.QrCodeScanner,
                                                contentDescription = "Scan Barcode",
                                                tint = EcoColors.Green600
                                            )
                                        }
                                    }
                                }
                            )
                        }
                        
                        // Lookup button when barcode is entered
                        AnimatedVisibility(
                            visible = !item.upc.isNullOrBlank() && item.name.isBlank(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Button(
                                onClick = {
                                    item.upc?.let { barcode ->
                                        viewModel.lookupBarcode(barcode)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EcoColors.Green100,
                                    contentColor = EcoColors.Green800
                                ),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLookingUpBarcode
                            ) {
                                if (isLookingUpBarcode) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = EcoColors.Green600
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Looking up product...")
                                } else {
                                    Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Look up product info from barcode")
                                }
                            }
                        }
                    }
                }

                // Pricing Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.AttachMoney,
                                contentDescription = null,
                                tint = EcoColors.Green600,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Pricing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Price Field
                        ModernPriceField(
                item = item,
                            onItemChange = { item = it }
                        )
                        
                        // Optional Discount Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (showDiscountSection)
                                        EcoColors.Green100
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { showDiscountSection = !showDiscountSection }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    if (showDiscountSection) Icons.Filled.LocalOffer else Icons.Outlined.LocalOffer,
                                    contentDescription = null,
                                    tint = if (showDiscountSection) EcoColors.Green600 else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column {
                                    Text(
                                        "Add Discount",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = if (showDiscountSection) EcoColors.Green800 else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Optional - Set a sale price",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = showDiscountSection,
                                onCheckedChange = { showDiscountSection = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EcoColors.Green600
                                )
                            )
                        }
                        
                        // Discount Fields (Collapsible)
                        AnimatedVisibility(
                            visible = showDiscountSection,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            ModernDiscountFields(
                                item = item,
                                onItemChange = { item = it }
                            )
                        }
                    }
                }

                // Inventory Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = EcoColors.Green600,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Inventory & Stock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            var stockInput by remember { mutableStateOf(item.stockQuantity.toString()) }
                            
                            OutlinedTextField(
                                value = stockInput,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() }) {
                                        stockInput = newValue
                                        item = item.copy(stockQuantity = newValue.toIntOrNull() ?: 1)
                                    }
                                },
                                label = { Text("Stock Quantity") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EcoColors.Green600,
                                    focusedLabelColor = EcoColors.Green600
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = item.unit.orEmpty(),
                                onValueChange = { item = item.copy(unit = it.ifBlank { null }) },
                                label = { Text("Unit") },
                                placeholder = { Text("e.g., kg, lb, each") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EcoColors.Green600,
                                    focusedLabelColor = EcoColors.Green600
                                ),
                                singleLine = true
                            )
                        }
                        
                        // Sold by Weight / Clearance Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FilterChip(
                                selected = item.soldByWeight,
                                onClick = { item = item.copy(soldByWeight = !item.soldByWeight) },
                                label = { Text("Sold by Weight") },
                                leadingIcon = {
                                    if (item.soldByWeight) {
                                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EcoColors.Green100,
                                    selectedLabelColor = EcoColors.Green800
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            
                            FilterChip(
                                selected = item.isClearance,
                                onClick = { item = item.copy(isClearance = !item.isClearance) },
                                label = { Text("Clearance Item") },
                                leadingIcon = {
                                    if (item.isClearance) {
                                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EcoColors.Orange500.copy(alpha = 0.2f),
                                    selectedLabelColor = EcoColors.Orange500
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Nutrition & Diet Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Restaurant,
                                contentDescription = null,
                                tint = EcoColors.Green600,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Nutrition & Diet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Scan Nutrition Button
                        OutlinedButton(
                            onClick = { showNutritionScanner = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = EcoColors.Green600
                            ),
                            border = BorderStroke(1.dp, EcoColors.Green600),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Nutrition Label")
                        }

                        // Nutrition Info Display
                        item.nutritionInfo?.let { info ->
                            NutritionInfoDisplay(nutritionInfo = info)
                        }

                        // Dietary Tags
                        Text(
                            "Dietary Options",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DietaryChip(
                                label = "Vegetarian",
                                icon = Icons.Outlined.Grass,
                                selected = item.isVegetarian,
                                onClick = {
                                val currentTags = item.tags?.toMutableList() ?: mutableListOf()
                                    if (item.isVegetarian) currentTags.remove("vegetarian") else currentTags.add("vegetarian")
                                    item = item.copy(tags = currentTags.distinct())
                                }
                            )
                            DietaryChip(
                                label = "Vegan",
                                icon = Icons.Outlined.Eco,
                                selected = item.isVegan,
                                onClick = {
                                    val currentTags = item.tags?.toMutableList() ?: mutableListOf()
                                    if (item.isVegan) currentTags.remove("vegan") else currentTags.add("vegan")
                                item = item.copy(tags = currentTags.distinct())
                            }
                        )
                            DietaryChip(
                                label = "Gluten-Free",
                                icon = Icons.Outlined.DoNotDisturb,
                                selected = item.isGlutenFree,
                                onClick = {
                                    val currentTags = item.tags?.toMutableList() ?: mutableListOf()
                                    if (item.isGlutenFree) currentTags.remove("gluten-free") else currentTags.add("gluten-free")
                                    item = item.copy(tags = currentTags.distinct())
                                }
                            )
                            DietaryChip(
                                label = "Organic",
                                icon = Icons.Outlined.Nature,
                                selected = item.tags?.contains("organic") == true,
                                onClick = {
                                    val currentTags = item.tags?.toMutableList() ?: mutableListOf()
                                    if (currentTags.contains("organic")) currentTags.remove("organic") else currentTags.add("organic")
                                    item = item.copy(tags = currentTags.distinct())
                                }
                            )
                        }

                        // Allergens
            AllergenInput(
                allergens = item.allergens,
                            onAllergensChange = { item = item.copy(allergens = it) }
            )
                    }
                }

            // Submit Button
            Button(
                onClick = {
                    if (item.name.isBlank()) {
                            Toast.makeText(context, "Please enter a product name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    val finalOriginalPrice = item.originalPrice ?: 0.0
                    if (finalOriginalPrice <= 0) {
                            Toast.makeText(context, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                        // Use original price as current if no discount was set
                        val finalCurrentPrice = if (showDiscountSection && item.currentPrice > 0) {
                            item.currentPrice
                        } else {
                            finalOriginalPrice
                        }
                        
                        // Build image list
                        val imageUrls = mutableListOf<String>()
                        capturedImagePath?.let { imageUrls.add(it) }
                        selectedImageUri?.toString()?.let { imageUrls.add(it) }
                    
                    Toast.makeText(context, "Creating item...", Toast.LENGTH_SHORT).show()
                    
                    viewModel.createItem(
                        name = item.name,
                        description = item.description,
                        category = item.category.toString(),
                        originalPrice = finalOriginalPrice,
                        currentPrice = finalCurrentPrice,
                        stockQuantity = item.stockQuantity,
                            unit = item.unit ?: "each",
                        barcode = item.upc,
                        sku = item.sku,
                            imageUrls = imageUrls.ifEmpty { null },
                        expiryDate = item.expiryDate,
                        bestBefore = item.bestBefore,
                        isClearance = item.isClearance,
                            tags = item.tags?.ifEmpty { null },
                        allergens = item.allergens
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcoColors.Green600
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = createItemState !is Resource.Loading
                ) {
                    if (createItemState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add Product",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Extra space at bottom to ensure button is visible above nav bar
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    // Fullscreen Dialog for Barcode Scanner
    if (showScanner) {
        Dialog(
            onDismissRequest = { showScanner = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                BarcodeScanScreen(
                    onBarcodeDetected = { code ->
                        item = item.copy(upc = code)
                        showScanner = false
                        // Auto-lookup the barcode
                        viewModel.lookupBarcode(code)
                    },
                    onBack = { showScanner = false }
                )
            }
        }
    }

    // Fullscreen Dialog for Camera
    if (showCamera) {
        Dialog(
            onDismissRequest = { showCamera = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                CameraCaptureScreen(
                    onPictureTaken = { file ->
                        capturedImagePath = file.absolutePath
                        selectedImageUri = null // Clear gallery image if camera used
                        item = item.copy(images = listOf(file.absolutePath))
                        showCamera = false
                        // Auto-scan the captured photo for product info
                        viewModel.scanProductImageFromFile(file.absolutePath)
                    },
                    onBack = { showCamera = false }
                )
            }
        }
    }

    // Fullscreen Dialog for Nutrition Scanner
    if (showNutritionScanner) {
        Dialog(
            onDismissRequest = { showNutritionScanner = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                NutritionScanScreen(
                    onTextFound = { detectedText ->
                        val parsedInfo = parseNutritionText(detectedText)
                        item = item.copy(nutritionInfo = parsedInfo)
                        showNutritionScanner = false
                    },
                    onBack = { showNutritionScanner = false }
                )
            }
        }
    }
}

@Composable
private fun DietaryChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EcoColors.Green100,
            selectedLabelColor = EcoColors.Green800,
            selectedLeadingIconColor = EcoColors.Green600
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selected: ItemCategory,
    onSelected: (ItemCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.name.lowercase().replaceFirstChar { it.titlecase() },
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EcoColors.Green600,
                focusedLabelColor = EcoColors.Green600
            ),
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ItemCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name.lowercase().replaceFirstChar { it.titlecase() }) },
                    onClick = {
                        onSelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ModernPriceField(
    item: Item,
    onItemChange: (Item) -> Unit
) {
    var priceInput by remember(item.originalPrice) {
        mutableStateOf(if (item.originalPrice == 0.0) "" else String.format("%.2f", item.originalPrice))
    }

    OutlinedTextField(
        value = priceInput,
        onValueChange = { newValue ->
            val regex = Regex("^\\d*\\.?\\d{0,2}$")
            if (newValue.matches(regex) || newValue.isEmpty()) {
                priceInput = newValue
                // Update item immediately on value change so create button works
                val newPrice = newValue.toDoubleOrNull() ?: 0.0
                onItemChange(
                    item.copy(
                        originalPrice = newPrice,
                        currentPrice = if (item.currentPrice <= 0 || item.currentPrice == item.originalPrice) newPrice else item.currentPrice
                    )
                )
            }
        },
        label = { Text("Price *") },
        placeholder = { Text("0.00") },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    // Format the price nicely when focus is lost
                    val finalPrice = priceInput.toDoubleOrNull() ?: 0.0
                    priceInput = if (finalPrice > 0.0) String.format("%.2f", finalPrice) else ""
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EcoColors.Green600,
            focusedLabelColor = EcoColors.Green600
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        prefix = { Text("$", fontWeight = FontWeight.Medium) },
        singleLine = true
    )
}

@Composable
private fun ModernDiscountFields(
    item: Item,
    onItemChange: (Item) -> Unit
) {
    var discountedPriceInput by remember(item.currentPrice) {
        mutableStateOf(if (item.currentPrice == 0.0 || item.currentPrice == item.originalPrice) "" else String.format("%.2f", item.currentPrice))
    }
    var discountPercentInput by remember(item.discountPercent) {
        mutableStateOf(if (item.discountPercent == null || item.discountPercent == 0.0) "" else item.discountPercent!!.toInt().toString())
    }

    fun String.safeToDouble(): Double = this.toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = discountedPriceInput,
                onValueChange = { newValue ->
                    val regex = Regex("^\\d*\\.?\\d{0,2}$")
                    if (newValue.matches(regex) || newValue.isEmpty()) {
                        discountedPriceInput = newValue
                        val newDiscountedPrice = newValue.safeToDouble()
                        val origPrice = item.originalPrice ?: 0.0
                        if (origPrice > 0 && newDiscountedPrice > 0 && newDiscountedPrice <= origPrice) {
                            val newPercent = ((origPrice - newDiscountedPrice) / origPrice * 100)
                            discountPercentInput = newPercent.toInt().toString()
                        } else {
                            discountPercentInput = ""
                        }
                    }
                },
                label = { Text("Sale Price") },
                modifier = Modifier
                    .weight(2f)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            val finalDiscountedPrice = discountedPriceInput.safeToDouble()
                            onItemChange(
                                item.copy(
                                    currentPrice = finalDiscountedPrice,
                                    discountPercent = if ((item.originalPrice ?: 0.0) > 0 && finalDiscountedPrice > 0) {
                                        ((item.originalPrice!! - finalDiscountedPrice) / item.originalPrice!! * 100)
                                    } else null
                                )
                            )
                            discountedPriceInput = if (finalDiscountedPrice > 0.0) String.format("%.2f", finalDiscountedPrice) else ""
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcoColors.Orange500,
                    focusedLabelColor = EcoColors.Orange500
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") },
                singleLine = true
            )

            OutlinedTextField(
                value = discountPercentInput,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        val percent = newValue.toIntOrNull() ?: 0
                        if (percent <= 100) {
                            discountPercentInput = newValue
                            val origPrice = item.originalPrice ?: 0.0
                            if (origPrice > 0 && percent > 0) {
                                val newDiscountedPrice = origPrice * (1 - percent / 100.0)
                                discountedPriceInput = String.format("%.2f", newDiscountedPrice)
                            } else {
                                discountedPriceInput = ""
                            }
                        }
                    } else if (newValue.isEmpty()) {
                        discountPercentInput = ""
                        discountedPriceInput = ""
                    }
                },
                label = { Text("% Off") },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            val finalPercent = discountPercentInput.toIntOrNull() ?: 0
                            val origPrice = item.originalPrice ?: 0.0
                            val finalDiscountedPrice = if (origPrice > 0 && finalPercent > 0) {
                                    origPrice * (1 - finalPercent / 100.0)
                                } else 0.0
                            onItemChange(
                                item.copy(
                                    currentPrice = finalDiscountedPrice,
                                    discountPercent = finalPercent.toDouble()
                                )
                            )
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcoColors.Orange500,
                    focusedLabelColor = EcoColors.Orange500
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("%") },
                singleLine = true
            )
        }
    }
}

@Composable
private fun NutritionInfoDisplay(nutritionInfo: NutritionInfo) {
    @Composable
    fun NutritionRow(label: String, value: Number?, unit: String) {
        if (value != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "$value$unit",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = EcoColors.Green600
                )
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = EcoColors.Green50
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Assessment,
                    contentDescription = null,
                    tint = EcoColors.Green600,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Nutrition Facts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = EcoColors.Green800
                )
            }
            Divider(color = EcoColors.Green200)
            NutritionRow("Calories", nutritionInfo.calories, "")
            NutritionRow("Protein", nutritionInfo.protein, "g")
            NutritionRow("Carbohydrates", nutritionInfo.carbohydrates, "g")
            NutritionRow("Fat", nutritionInfo.fat, "g")
            NutritionRow("Fiber", nutritionInfo.fiber, "g")
            NutritionRow("Sugar", nutritionInfo.sugar, "g")
            NutritionRow("Sodium", nutritionInfo.sodium, "mg")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AllergenInput(
    allergens: List<String>?,
    onAllergensChange: (List<String>) -> Unit
) {
    var currentAllergenInput by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Allergens",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

    if (!allergens.isNullOrEmpty()) {
        FlowRow(
                modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allergens.forEach { allergen ->
                InputChip(
                    selected = false,
                        onClick = { },
                    label = { Text(allergen) },
                    trailingIcon = {
                        Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                            modifier = Modifier
                                .clickable {
                                        onAllergensChange(allergens.filter { it != allergen })
                                }
                                .size(18.dp)
                        )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = EcoColors.Red500.copy(alpha = 0.1f),
                            labelColor = EcoColors.Red500
                        )
                    )
                }
            }
        }

    val addAllergen = {
        if (currentAllergenInput.isNotBlank()) {
                onAllergensChange(
                    (allergens.orEmpty() + currentAllergenInput.trim()).distinct()
                )
                currentAllergenInput = ""
            }
        }

    OutlinedTextField(
        value = currentAllergenInput,
        onValueChange = { currentAllergenInput = it },
            label = { Text("Add Allergen") },
            placeholder = { Text("e.g., Peanuts, Dairy") },
        modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EcoColors.Green600,
                focusedLabelColor = EcoColors.Green600
            ),
        singleLine = true,
        trailingIcon = {
            IconButton(
                    onClick = { addAllergen() },
                enabled = currentAllergenInput.isNotBlank()
            ) {
                    Icon(
                        Icons.Default.AddCircle,
                        contentDescription = "Add",
                        tint = if (currentAllergenInput.isNotBlank()) EcoColors.Green600 else MaterialTheme.colorScheme.onSurfaceVariant
                    )
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { addAllergen() })
    )
    }
}

private fun parseNutritionText(text: String): NutritionInfo {
    fun findValue(keyword: String): Double? {
        val regex = Regex("""\b$keyword\b\s*.*?(\d+(\.\d+)?)""", RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    return NutritionInfo(
        calories = findValue("Calories")?.toInt(),
        protein = findValue("Protein"),
        carbohydrates = findValue("Carbohydrate"),
        fat = findValue("Fat"),
        fiber = findValue("Fiber"),
        sugar = findValue("Sugar"),
        sodium = findValue("Sodium")
    )
}
