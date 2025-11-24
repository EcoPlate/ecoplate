package com.example.eco_plate.ui.newItem

import android.R.attr.label
import android.R.attr.onClick
import androidx.compose.animation.core.copy
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.data.models.ItemCategory
import com.example.eco_plate.data.models.NutritionInfo
import com.example.eco_plate.data.models.Store
import com.example.eco_plate.ui.components.EcoColors
import com.example.eco_plate.ui.components.Rounded
import com.example.eco_plate.ui.components.Spacing
import com.example.eco_plate.utils.Resource
import com.google.gson.annotations.SerializedName
import java.io.File
import kotlin.String
import kotlin.text.all
import kotlin.text.isDigit
import kotlin.text.orEmpty



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewItemScreen (
    viewModel: NewItemViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
){
    val createItemState by viewModel.createItemState.observeAsState()
    
    var showScanner by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var showNutritionScanner by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Handle item creation result
    LaunchedEffect(createItemState) {
        when (createItemState) {
            is Resource.Success<*> -> {
                // Item created successfully
                Toast.makeText(context, "Item created successfully!", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
            is Resource.Error -> {
                val errorMessage = (createItemState as Resource.Error).message ?: "Failed to create item"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
            else -> {}
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
                discountPercent = 0.0,
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
                createdAt = "", // Will be set by backend
                updatedAt = "", // Will be set by backend
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
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add a New Store Item",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {

        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ){paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            // Photo section
            Text("Enter Item Info", style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (!item.imageUrl.isNullOrEmpty()) {
                    // Use Coil or another image loading library in a real app
                    Image(
                        bitmap = android.graphics.BitmapFactory.decodeFile(item.imageUrl).asImageBitmap(),
                        contentDescription = "Item photo",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("No photo yet", color = Color.DarkGray)
                }
                Button(onClick = { showCamera = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ){
                    Text("Take Photo")
                }
            }
            // UPC
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = item.upc.orEmpty(),
                    onValueChange = { newValue -> item = item.copy(upc = newValue.ifBlank { null }) }, // FIX: Update UPC
                    label = { Text("UPC") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showScanner = true }) {
                    Icon(Icons.Default.QrCodeScanner, "Scan Barcode")
                }

            }

            //Item Name input
            OutlinedTextField(
                value = item.name,
                onValueChange = {newValue -> item = item.copy(name = newValue)},
                label = {Text("Item Name *") },
                modifier = Modifier.fillMaxWidth()
            )

            //Brand, PLU row
            var pluInput by remember(item.plu) {
                // If item.plu is not null, convert it to a string for the text field, otherwise use an empty string
                mutableStateOf(item.plu?.toString() ?: "")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                //Item Brand Input
                OutlinedTextField(
                    value = item.brand.orEmpty(),
                    onValueChange = { newValue -> item = item.copy(brand = newValue.ifBlank { null }) },
                    label = { Text("Brand") },
                    modifier = Modifier.weight(2f)
                )
                OutlinedTextField(
                    value = pluInput,
                    onValueChange = { newValue->
                        if (newValue.all { it.isDigit() }) {
                            pluInput = newValue
                        } },
                    label = { Text("PLU ") },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                // When the user leaves the field, update the real item state
                                val finalPlu = pluInput.toIntOrNull()
                                item = item.copy(plu = finalPlu)
                                // Also, re-sync the text field with the formatted state
                                pluInput = finalPlu?.toString() ?: ""
                            }
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // category dropdown and Unit
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryDropdown(
                    selected = try { ItemCategory.valueOf(item.category ?: "OTHER") } catch(e: Exception) { ItemCategory.OTHER },
                    onSelected = { newCategory -> item = item.copy(category = newCategory.name)},
                    modifier = Modifier.weight(2f)
                )
//                //Item unit input (redundant)?
//                OutlinedTextField(
//                    value = item.unit.orEmpty(),
//                    onValueChange = { newValue -> item = item.copy(unit = newValue.ifBlank { null }) },
//                    label = { Text("Unit") },
//                    modifier = Modifier.weight(1f)
//                )
            }

            //Item Description Input
            OutlinedTextField(
                value = item.description.orEmpty(),
                onValueChange = { newValue -> item = item.copy(description = newValue.ifBlank { null }) },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            // Original Price Field
            OriginalPriceField(
                item = item,
                onItemChange = { updatedItem -> item = updatedItem }
            )

            //Discount Price or %
            DiscountPricingFields(item = item,
                onItemChange = { updatedItem -> item = updatedItem }
            )

            Text("Nutrition/Diet", style = MaterialTheme.typography.titleMedium)
            //Nutrition Info
            Button(onClick = { showNutritionScanner = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ){
                Text("Scan Nutrition Facts")
            }
            //Display Info if available
            item.nutritionInfo?.let { info ->
                NutritionInfoDisplay(nutritionInfo = info)
            }

            // Checkboxes for dietary restrictions
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = item.isVegetarian,
                            onCheckedChange = { isChecked ->
                                val currentTags = item.tags?.toMutableList() ?: mutableListOf()
                                if (isChecked) {
                                    currentTags.add("vegetarian")
                                } else {
                                    currentTags.remove("vegetarian")
                                }
                                item = item.copy(tags = currentTags.distinct())
                            }
                        )
                        Text("Vegetarian")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = item.isVegan,
                            onCheckedChange = { isChecked ->
                                val currentTags = item.tags?.toMutableList() ?: mutableListOf()
                                if (isChecked) {
                                    currentTags.add("vegan")
                                } else {
                                    currentTags.remove("vegan")
                                }
                                item = item.copy(tags = currentTags.distinct())
                            }
                        )
                        Text("Vegan")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = item.isGlutenFree,
                            onCheckedChange = { isChecked ->
                                val currentTags = item.tags?.toMutableList() ?: mutableListOf()
                                if (isChecked) {
                                    currentTags.add("gluten-free")
                                } else {
                                    currentTags.remove("gluten-free")
                                }
                                item = item.copy(tags = currentTags.distinct())
                            }
                        )
                        Text("Gluten-Free")
                    }
                }
            }
            //Allergens Input
            //Allergens Input
            AllergenInput(
                allergens = item.allergens,
                onAllergensChange = { updatedList ->
                    item = item.copy(allergens = updatedList)
                }
            )


            // Submit Button
            Button(
                onClick = {
                    // Validate required fields
                    if (item.name.isBlank()) {
                        // Show error - name is required
                        Toast.makeText(context, "Item name is required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    // Check if we have a valid original price
                    val finalOriginalPrice = item.originalPrice ?: 0.0
                    
                    if (finalOriginalPrice <= 0) {
                        // Show error - price must be positive
                        Toast.makeText(context, "Original price is required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    // If no discount price is set, use original price as current price
                    val finalCurrentPrice = if (item.currentPrice > 0) item.currentPrice else finalOriginalPrice
                    
                    // Build tags list from dietary preferences
                    val tags = mutableListOf<String>()
                    item.tags?.forEach { tags.add(it) }
                    
                    // Convert image URL to list if present
                    val imageUrls = item.images ?: emptyList()
                    
                    Toast.makeText(context, "Creating item...", Toast.LENGTH_SHORT).show()
                    
                    // Call the viewModel to create the item
                    viewModel.createItem(
                        name = item.name,
                        description = item.description,
                        category = item.category.toString(),
                        originalPrice = finalOriginalPrice,
                        currentPrice = finalCurrentPrice,
                        stockQuantity = item.stockQuantity,
                        unit = item.unit ?: "unit",
                        barcode = item.upc,
                        sku = item.sku,
                        imageUrls = imageUrls,
                        expiryDate = item.expiryDate,
                        bestBefore = item.bestBefore,
                        isClearance = item.isClearance,
                        tags = tags.ifEmpty { null },
                        allergens = item.allergens
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save Item", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
        if (showScanner) {
            BarcodeScanScreen(
                onBarcodeDetected = { code ->
                    item = item.copy(upc = code) // Correct state update
                    showScanner = false
                },
                onBack = { showScanner = false }
            )
        }

        if (showCamera) {
            CameraCaptureScreen(
                onPictureTaken = { file ->
                    item = item.copy(images = listOf(file.absolutePath)) // Use images list
                    showCamera = false
                },
                onBack = { showCamera = false }
            )
        }
        if (showNutritionScanner) {
            NutritionScanScreen(
                onTextFound = { detectedText ->
                    // 1. Parse the raw text into a NutritionInfo object
                    val parsedInfo = parseNutritionText(detectedText)

                    // 2. Update the item's state with the new nutrition info
                    item = item.copy(nutritionInfo = parsedInfo)

                    // Optional: You can also still fill the description for user reference
                    item = item.copy(description = "Nutrition facts scanned and parsed.")

                    // 3. Close the scanner
                    showNutritionScanner = false
                },
                onBack = { showNutritionScanner = false }
            )
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selected: ItemCategory,
    onSelected: (ItemCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    // This is the state for the ExposedDropdownMenuBox
    var expanded by remember { mutableStateOf(false) }

    // This is the component designed for this exact purpose
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier // Pass the modifier to the container
    ) {
        // The read-only TextField that shows the current selection
        OutlinedTextField(
            value = selected.name.replaceFirstChar { it.titlecase() },
            onValueChange = {}, // No-op, it's read-only
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = {
                // The icon inside the TextField
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            // This modifier is crucial for the dropdown to anchor correctly
            modifier = Modifier.menuAnchor()
        )

        // This is the actual dropdown menu that appears
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ItemCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name.replaceFirstChar { it.titlecase() }) },
                    onClick = {
                        onSelected(category)
                        expanded = false // Close the menu after selection
                    }
                )
            }
        }
    }
}

@Composable
fun OriginalPriceField(
    item: Item,
    onItemChange: (Item) -> Unit
) {
    var priceInput by remember(item.originalPrice) {        // Initialize the text field. Show "0.0" only if it's the actual saved value.
        // Otherwise, show the number or an empty string.
        mutableStateOf(
            if (item.originalPrice == 0.0) "" else item.originalPrice.toString()
        )
    }

    Column {
        Text("Price", style = MaterialTheme.typography.titleMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = priceInput,
                onValueChange = { newValue ->
                    // This is our "forced limiter" regex
                    val regex = Regex("^\\d*\\.?\\d{0,2}$")
                    if (newValue.matches(regex) || newValue.isEmpty()) {
                        priceInput = newValue
                        // IMPORTANT: Only update the local string state while typing
                    }
                },
                label = { Text("Original Price*") },
                modifier = Modifier
                    .weight(2f)
                    .onFocusChanged { focusState ->
                        // When the user leaves the field, format the number
                        if (!focusState.isFocused) {
                            val finalPrice = priceInput.toDoubleOrNull() ?: 0.0
                            // Update the official Item state - set both originalPrice and currentPrice if currentPrice is 0
                            onItemChange(item.copy(
                                originalPrice = finalPrice,
                                currentPrice = if (item.currentPrice <= 0) finalPrice else item.currentPrice
                            ))
                            // Format the visible text to have two decimal places
                            priceInput =
                                if (finalPrice > 0.0) String.format("%.2f", finalPrice) else ""
                        }
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text("$") }
            )
            // "Sold by Weight" Checkbox
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = item.soldByWeight,
                    onCheckedChange = { isChecked ->
                        onItemChange(item.copy(soldByWeight = isChecked))
                    }
                )
                Text("/lb")
            }
        }
    }
}

@Composable
fun DiscountPricingFields(
    item: Item,
    onItemChange: (Item) -> Unit
) {
    // Local string state for the text field inputs to prevent cursor jumping
    var discountedPriceInput by remember(item.discountedPrice) {
        mutableStateOf(if (item.discountedPrice == 0.0) "" else item.discountedPrice.toString())
    }
    var discountPercentInput by remember(item.discountPercentage) {
        mutableStateOf(if (item.discountPercentage == 0.0) "" else item.discountPercentage.toInt().toString())
    }

    // --- The redundant 'originalPriceInput' state has been removed ---

    // A simple helper to parse text to a double safely
    fun String.safeToDouble(): Double = this.toDoubleOrNull() ?: 0.0

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Discount", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Discounted Price
            OutlinedTextField(
                value = discountedPriceInput,
                onValueChange = { newValue ->
                    // Forced limiter for a valid price format
                    val regex = Regex("^\\d*\\.?\\d{0,2}$")
                    if (newValue.matches(regex) || newValue.isEmpty()) {
                        discountedPriceInput = newValue
                        // Calculate percentage on the fly while typing price
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
                label = { Text("Discounted Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text("$") },
                modifier = Modifier
                    .weight(2f)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            // When user leaves the field, format the number and update the state
                            val finalDiscountedPrice = discountedPriceInput.safeToDouble()
                            onItemChange(
                                item.copy(
                                    currentPrice = finalDiscountedPrice,
                                    discountPercent = if ((item.originalPrice ?: 0.0) > 0 && finalDiscountedPrice > 0) {
                                        ((item.originalPrice!! - finalDiscountedPrice) / item.originalPrice!! * 100)
                                    } else 0.0
                                )
                            )
                            // Format the visible text
                            discountedPriceInput = if (finalDiscountedPrice > 0.0) String.format(
                                "%.2f",
                                finalDiscountedPrice
                            ) else ""
                        }
                    }
            )

            // Discount Percentage
            OutlinedTextField(
                value = discountPercentInput,
                onValueChange = { newValue ->
                    // Allow only digits and limit to 1-100
                    if (newValue.all { it.isDigit() }) {
                        val percent = newValue.toIntOrNull() ?: 0
                        if (percent <= 100) {
                            discountPercentInput = newValue
                            // Calculate discounted price on the fly while typing percent
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
                label = { Text("Percent") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("%") },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            // When user leaves the field, commit the final state
                            val finalPercent = discountPercentInput.toIntOrNull() ?: 0
                            val origPrice = item.originalPrice ?: 0.0
                            val finalDiscountedPrice =
                                if (origPrice > 0 && finalPercent > 0) {
                                    origPrice * (1 - finalPercent / 100.0)
                                } else 0.0
                            onItemChange(
                                item.copy(
                                    currentPrice = finalDiscountedPrice,
                                    discountPercent = finalPercent.toDouble()
                                )
                            )
                        }
                    }
            )
        }
    }
}

@Composable
private fun NutritionInfoDisplay(nutritionInfo: NutritionInfo) {
    // A helper to format and display each nutrition fact
    @Composable
    fun NutritionFact(label: String, value: Number?, unit: String) {
        if (value != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                Text("$value$unit", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    // A Card provides a nice visual grouping for the information
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Scanned Nutrition Facts", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            // Display each piece of data from the NutritionInfo object
            NutritionFact("Calories", nutritionInfo.calories, "")
            NutritionFact("Protein", nutritionInfo.protein, "g")
            NutritionFact("Carbohydrates", nutritionInfo.carbohydrates, "g")
            NutritionFact("Fat", nutritionInfo.fat, "g")
            NutritionFact("Fiber", nutritionInfo.fiber, "g")
            NutritionFact("Sugar", nutritionInfo.sugar, "g")
            NutritionFact("Sodium", nutritionInfo.sodium, "mg")
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

    // Title for the section
    Text("Allergens", style = MaterialTheme.typography.titleMedium)

    // 1. Display selected allergens as chips
    if (!allergens.isNullOrEmpty()) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allergens.forEach { allergen ->
                InputChip(
                    selected = false,
                    onClick = { /* No-op */ },
                    label = { Text(allergen) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove allergen",
                            modifier = Modifier
                                .clickable {
                                    val updatedAllergens = allergens
                                        .toMutableList()
                                        .apply { remove(allergen) }
                                    onAllergensChange(updatedAllergens)
                                }
                                .size(18.dp)
                        )
                    }
                )
            }
        }
    }

    // Function to handle adding a new allergen
    val addAllergen = {
        if (currentAllergenInput.isNotBlank()) {
            val updatedAllergens = allergens.orEmpty().toMutableList()
                .apply { add(currentAllergenInput.trim()) }
            onAllergensChange(updatedAllergens)
            currentAllergenInput = "" // Clear the input field
        }
    }

    // 2. Text field to add a new allergen
    OutlinedTextField(
        value = currentAllergenInput,
        onValueChange = { currentAllergenInput = it },
        label = { Text("Add an Allergen") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        trailingIcon = {
            IconButton(
                onClick = addAllergen,
                enabled = currentAllergenInput.isNotBlank()
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add Allergen")
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { addAllergen() })
    )
}

//Helper function to get nutrition info -> NutritionInfo
private fun parseNutritionText(text: String): NutritionInfo {
    // Regex to find a number (integer or double) that may have "g", "mg", etc. after it.
    // It captures only the numeric part.
    fun findValue(keyword: String): Double? {
        // Matches the keyword (case-insensitive), followed by any characters, then captures the first number found.
        val regex = Regex("""\b$keyword\b\s*.*?(\d+(\.\d+)?)""", RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    val calories = findValue("Calories")?.toInt() // Calories are usually integers
    val protein = findValue("Protein")
    val carbohydrates = findValue("Carbohydrate") // Handles "Total Carbohydrate"
    val fat = findValue("Fat") // Handles "Total Fat"
    val fiber = findValue("Fiber") // Handles "Dietary Fiber"
    val sugar = findValue("Sugar") // Handles "Total Sugars" or "Includes Xg Added Sugars"
    val sodium = findValue("Sodium")

    return NutritionInfo(
        calories = calories,
        protein = protein,
        carbohydrates = carbohydrates,
        fat = fat,
        fiber = fiber,
        sugar = sugar,
        sodium = sodium
    )
}



