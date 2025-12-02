package com.example.eco_plate.ui.newItem

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.api.BarcodeApi
import com.example.eco_plate.data.api.OpenFoodFactsProduct
import com.example.eco_plate.data.local.TokenManager
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.data.models.NutritionInfo
import com.example.eco_plate.data.repository.InventoryRepository
import com.example.eco_plate.data.repository.StoreRepository
import com.example.eco_plate.utils.Resource
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing product info from barcode lookup
 */
data class BarcodeProductInfo(
    val barcode: String,
    val name: String?,
    val brand: String?,
    val description: String?,
    val category: String?,
    val imageUrl: String?,
    val nutritionInfo: NutritionInfo?,
    val allergens: List<String>?,
    val tags: List<String>?
)

/**
 * Result from scanning a product image
 */
data class ImageScanResult(
    val barcode: String? = null,
    val extractedText: String? = null,
    val productName: String? = null,
    val brand: String? = null
)

@HiltViewModel
class NewItemViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val storeRepository: StoreRepository,
    private val tokenManager: TokenManager,
    private val barcodeApi: BarcodeApi,
    @ApplicationContext private val context: Context
): ViewModel() {
    
    private val _createItemState = MutableLiveData<Resource<Item>>()
    val createItemState: LiveData<Resource<Item>> = _createItemState
    
    private val _storeId = MutableLiveData<String?>()
    val storeId: LiveData<String?> = _storeId
    
    private val _barcodeLookupState = MutableLiveData<Resource<BarcodeProductInfo>>()
    val barcodeLookupState: LiveData<Resource<BarcodeProductInfo>> = _barcodeLookupState
    
    private val _imageScanState = MutableLiveData<Resource<ImageScanResult>>()
    val imageScanState: LiveData<Resource<ImageScanResult>> = _imageScanState
    
    // ML Kit scanners
    private val barcodeScanner = BarcodeScanning.getClient()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    init {
        loadStoreId()
    }
    
    /**
     * Scan an image for barcodes and text to auto-populate product info
     */
    fun scanProductImage(imageUri: Uri) {
        _imageScanState.postValue(Resource.Loading())
        
        try {
            val inputImage = InputImage.fromFilePath(context, imageUri)
            
            // First, try to detect barcodes
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val detectedBarcode = barcodes
                        .filter { it.valueType == Barcode.TYPE_PRODUCT }
                        .firstOrNull()?.rawValue
                        ?: barcodes.firstOrNull()?.rawValue
                    
                    if (detectedBarcode != null) {
                        Log.d("NewItemViewModel", "Barcode detected in image: $detectedBarcode")
                        _imageScanState.postValue(Resource.Success(ImageScanResult(barcode = detectedBarcode)))
                        // Automatically look up the barcode
                        lookupBarcode(detectedBarcode)
                    } else {
                        // No barcode found, try text recognition
                        scanImageForText(inputImage)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("NewItemViewModel", "Barcode scanning failed", e)
                    // Try text recognition as fallback
                    scanImageForText(inputImage)
                }
        } catch (e: Exception) {
            Log.e("NewItemViewModel", "Image scan error", e)
            _imageScanState.postValue(Resource.Error("Failed to scan image: ${e.localizedMessage}"))
        }
    }
    
    /**
     * Scan a captured photo file for product info
     */
    fun scanProductImageFromFile(filePath: String) {
        _imageScanState.postValue(Resource.Loading())
        
        try {
            val bitmap = BitmapFactory.decodeFile(filePath)
            if (bitmap == null) {
                _imageScanState.postValue(Resource.Error("Failed to load image"))
                return
            }
            
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            
            // First, try to detect barcodes
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val detectedBarcode = barcodes
                        .filter { it.valueType == Barcode.TYPE_PRODUCT }
                        .firstOrNull()?.rawValue
                        ?: barcodes.firstOrNull()?.rawValue
                    
                    if (detectedBarcode != null) {
                        Log.d("NewItemViewModel", "Barcode detected in photo: $detectedBarcode")
                        _imageScanState.postValue(Resource.Success(ImageScanResult(barcode = detectedBarcode)))
                        // Automatically look up the barcode
                        lookupBarcode(detectedBarcode)
                    } else {
                        // No barcode found, try text recognition
                        scanImageForText(inputImage)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("NewItemViewModel", "Barcode scanning failed", e)
                    // Try text recognition as fallback
                    scanImageForText(inputImage)
                }
        } catch (e: Exception) {
            Log.e("NewItemViewModel", "Photo scan error", e)
            _imageScanState.postValue(Resource.Error("Failed to scan photo: ${e.localizedMessage}"))
        }
    }
    
    /**
     * Try to extract text from an image
     */
    private fun scanImageForText(inputImage: InputImage) {
        textRecognizer.process(inputImage)
            .addOnSuccessListener { textResult ->
                if (textResult.text.isNotBlank()) {
                    val extractedInfo = parseProductTextFromImage(textResult.text)
                    Log.d("NewItemViewModel", "Text extracted: ${textResult.text.take(200)}")
                    _imageScanState.postValue(Resource.Success(extractedInfo))
                } else {
                    _imageScanState.postValue(Resource.Error("No product information found. Try taking a clearer photo with the barcode visible."))
                }
            }
            .addOnFailureListener { e ->
                Log.e("NewItemViewModel", "Text recognition failed", e)
                _imageScanState.postValue(Resource.Error("Failed to scan image for text: ${e.localizedMessage}"))
            }
    }
    
    /**
     * Parse extracted text to find product name and brand
     */
    private fun parseProductTextFromImage(text: String): ImageScanResult {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        
        // Common brand keywords to look for
        val brandKeywords = listOf("by ", "from ", "brand:", "made by")
        
        var productName: String? = null
        var brand: String? = null
        
        // First non-trivial line is often the product name
        val potentialName = lines.firstOrNull { line -> 
            line.length > 3 && !line.all { c -> c.isDigit() || c == '.' || c == '$' } 
        }
        productName = potentialName?.take(100)
        
        // Look for brand indicators
        for (line in lines) {
            val lowerLine = line.lowercase()
            for (keyword in brandKeywords) {
                if (lowerLine.contains(keyword)) {
                    val idx = lowerLine.indexOf(keyword)
                    brand = line.substring(idx + keyword.length).trim().take(50)
                    break
                }
            }
            if (brand != null) break
        }
        
        // If no brand found, check if second line looks like a brand
        if (brand == null && lines.size > 1) {
            val secondLine = lines[1]
            if (secondLine.length in 2..30 && !secondLine.contains("$") && !secondLine.all { it.isDigit() }) {
                brand = secondLine
            }
        }
        
        return ImageScanResult(
            extractedText = text.take(500),
            productName = productName,
            brand = brand
        )
    }
    
    /**
     * Clear image scan state
     */
    fun clearImageScan() {
        _imageScanState.postValue(null)
    }
    
    /**
     * Look up product information from a barcode using Open Food Facts API
     */
    fun lookupBarcode(barcode: String) {
        viewModelScope.launch {
            _barcodeLookupState.value = Resource.Loading()
            
            try {
                Log.d("NewItemViewModel", "Looking up barcode: $barcode")
                val response = barcodeApi.getProductByBarcode(barcode)
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    
                    if (data.status == 1 && data.product != null) {
                        val product = data.product
                        val productInfo = mapOpenFoodFactsProduct(barcode, product)
                        Log.d("NewItemViewModel", "Found product: ${productInfo.name}")
                        _barcodeLookupState.value = Resource.Success(productInfo)
                    } else {
                        Log.d("NewItemViewModel", "Product not found in database")
                        _barcodeLookupState.value = Resource.Error("Product not found. You can enter the details manually.")
                    }
                } else {
                    Log.e("NewItemViewModel", "API error: ${response.message()}")
                    _barcodeLookupState.value = Resource.Error("Failed to lookup barcode: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("NewItemViewModel", "Barcode lookup error", e)
                _barcodeLookupState.value = Resource.Error("Error looking up barcode: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Clear barcode lookup state
     */
    fun clearBarcodeLookup() {
        _barcodeLookupState.value = null
    }
    
    private fun mapOpenFoodFactsProduct(barcode: String, product: OpenFoodFactsProduct): BarcodeProductInfo {
        // Map nutrition info
        val nutritionInfo = product.nutriments?.let { n ->
            NutritionInfo(
                calories = n.energy_kcal_100g?.toInt(),
                protein = n.proteins_100g,
                carbohydrates = n.carbohydrates_100g,
                fat = n.fat_100g,
                fiber = n.fiber_100g,
                sugar = n.sugars_100g,
                sodium = n.sodium_100g?.times(1000) // Convert to mg
            )
        }
        
        // Parse allergens
        val allergens = product.allergens_tags?.map { tag ->
            tag.removePrefix("en:").replace("-", " ").replaceFirstChar { it.uppercase() }
        }
        
        // Parse labels as tags (organic, vegan, etc.)
        val tags = product.labels_tags?.mapNotNull { tag ->
            val cleanTag = tag.removePrefix("en:").lowercase()
            when {
                cleanTag.contains("organic") -> "organic"
                cleanTag.contains("vegan") -> "vegan"
                cleanTag.contains("vegetarian") -> "vegetarian"
                cleanTag.contains("gluten-free") || cleanTag.contains("no-gluten") -> "gluten-free"
                else -> null
            }
        }?.distinct()
        
        // Map category
        val category = product.categories?.split(",")?.firstOrNull()?.trim()?.uppercase()?.let {
            when {
                it.contains("BEVERAGE") || it.contains("DRINK") -> "BEVERAGES"
                it.contains("DAIRY") || it.contains("MILK") || it.contains("CHEESE") -> "DAIRY"
                it.contains("BREAD") || it.contains("BAKERY") -> "BAKERY"
                it.contains("MEAT") -> "MEAT"
                it.contains("FRUIT") || it.contains("VEGETABLE") || it.contains("PRODUCE") -> "PRODUCE"
                it.contains("SNACK") -> "SNACKS"
                it.contains("FROZEN") -> "FROZEN"
                it.contains("CEREAL") || it.contains("BREAKFAST") -> "PANTRY"
                else -> "OTHER"
            }
        } ?: "OTHER"
        
        return BarcodeProductInfo(
            barcode = barcode,
            name = product.product_name,
            brand = product.brands,
            description = product.generic_name ?: product.ingredients_text?.take(200),
            category = category,
            imageUrl = product.image_front_url ?: product.image_url,
            nutritionInfo = nutritionInfo,
            allergens = allergens,
            tags = tags
        )
    }
    
    private fun loadStoreId() {
        viewModelScope.launch {
            // First check if we have a saved store ID
            val savedStoreId = tokenManager.storeId.first()
            if (savedStoreId != null) {
                _storeId.value = savedStoreId
            } else {
                // If not, load the user's stores
                storeRepository.getMyStores()
                    .onEach { result ->
                        if (result is Resource.Success) {
                            val stores = result.data
                            if (!stores.isNullOrEmpty()) {
                                // Use the first store
                                val firstStore = stores.first()
                                _storeId.value = firstStore.id
                                tokenManager.saveStoreId(firstStore.id)
                            }
                        }
                    }
                    .launchIn(viewModelScope)
            }
        }
    }
    
    fun createItem(
        name: String,
        description: String?,
        category: String,
        originalPrice: Double,
        currentPrice: Double,
        stockQuantity: Int,
        unit: String = "unit",
        barcode: String? = null,
        sku: String? = null,
        imageUrls: List<String>? = null,
        expiryDate: String? = null,
        bestBefore: String? = null,
        isClearance: Boolean = false,
        tags: List<String>? = null,
        allergens: List<String>? = null,
        calories: Int? = null,
        protein: Double? = null,
        carbs: Double? = null,
        fat: Double? = null,
        fiber: Double? = null,
        sugar: Double? = null,
        sodium: Double? = null
    ) {
        viewModelScope.launch {
            // Get or fetch store ID
            var currentStoreId = _storeId.value
            
            if (currentStoreId == null) {
                // Try to fetch the store for this user
                _createItemState.value = Resource.Loading()
                
                try {
                    // Get first emission from the flow
                    val result = storeRepository.getMyStores().first()
                    
                    when (result) {
                        is Resource.Success -> {
                            val stores = result.data
                            if (!stores.isNullOrEmpty()) {
                                currentStoreId = stores.first().id
                                _storeId.value = currentStoreId
                                tokenManager.saveStoreId(currentStoreId)
                            } else {
                                // No stores found - this user needs to create a store first
                                _createItemState.value = Resource.Error("No store found. Store owners need to create a store first through the website or contact support.")
                                return@launch
                            }
                        }
                        is Resource.Error -> {
                            _createItemState.value = Resource.Error("Failed to fetch store: ${result.message}")
                            return@launch
                        }
                        is Resource.Loading -> {
                            // Should not happen with first()
                            _createItemState.value = Resource.Error("Unexpected loading state")
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    _createItemState.value = Resource.Error("Error fetching store: ${e.message}")
                    return@launch
                }
            }
            
            // If we still don't have a store ID, return
            if (currentStoreId == null) {
                _createItemState.value = Resource.Error("Unable to determine store. Please try logging out and back in.")
                return@launch
            }
            val itemData = mutableMapOf<String, Any>(
                "storeId" to currentStoreId,
                "name" to name,
                "category" to category.toUpperCase(),  // Backend expects uppercase categories
                "originalPrice" to originalPrice,
                "currentPrice" to currentPrice,
                "stockQuantity" to stockQuantity,
                "unit" to unit,
                "isAvailable" to true
            )
            
            description?.let { itemData["description"] = it }
            barcode?.let { itemData["barcode"] = it }
            sku?.let { itemData["sku"] = it }
            imageUrls?.let { itemData["images"] = it }
            expiryDate?.let { itemData["expiryDate"] = it }
            bestBefore?.let { itemData["bestBefore"] = it }
            if (isClearance) { itemData["isClearance"] = true }
            tags?.let { itemData["tags"] = it }
            allergens?.let { itemData["allergens"] = it }
            
            // Add nutrition info if provided
            if (calories != null || protein != null || carbs != null || fat != null) {
                val nutritionInfo = mutableMapOf<String, Any?>()
                calories?.let { nutritionInfo["calories"] = it }
                protein?.let { nutritionInfo["protein"] = it }
                carbs?.let { nutritionInfo["carbs"] = it }
                fat?.let { nutritionInfo["fat"] = it }
                fiber?.let { nutritionInfo["fiber"] = it }
                sugar?.let { nutritionInfo["sugar"] = it }
                sodium?.let { nutritionInfo["sodium"] = it }
                itemData["nutritionInfo"] = nutritionInfo
            }
            
            // Create the item with the store ID
            inventoryRepository.createItem(itemData)
                .onEach { result ->
                    _createItemState.value = result
                }
                .launchIn(viewModelScope)
        }
    }
}