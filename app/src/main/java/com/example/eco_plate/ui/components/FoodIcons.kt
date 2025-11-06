package com.example.eco_plate.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.eco_plate.R

/**
 * Food and store category icons for the EcoPlate app.
 * These icons are used throughout the app to represent different food categories,
 * store types, and other food-related elements.
 */
object FoodIcons {
    // Food Categories
    val Vegetables = Icons.Outlined.Eco
    val Fruits = Icons.Outlined.LocalFlorist
    val Bread = Icons.Outlined.BakeryDining
    val Dairy = Icons.Outlined.EmojiFoodBeverage
    val Meat = Icons.Outlined.SetMeal
    val Seafood = Icons.Outlined.Restaurant
    val Snacks = Icons.Outlined.Cookie
    val Beverages = Icons.Outlined.LocalBar
    val Frozen = Icons.Outlined.AcUnit
    val Pantry = Icons.Outlined.Kitchen
    val Organic = Icons.Outlined.Nature
    val Vegan = Icons.Outlined.Spa
    
    // Store Types
    val Grocery = Icons.Outlined.Store
    val Restaurant = Icons.Outlined.Restaurant
    val Bakery = Icons.Outlined.BakeryDining
    val Cafe = Icons.Outlined.LocalCafe
    val Market = Icons.Outlined.Storefront
    val Pharmacy = Icons.Outlined.LocalPharmacy
    val Convenience = Icons.Outlined.LocalConvenienceStore
    
    // Order & Delivery
    val Delivery = Icons.Outlined.DeliveryDining
    val Pickup = Icons.Outlined.DirectionsCar
    val OrderHistory = Icons.Outlined.Receipt
    val Track = Icons.Outlined.LocationOn
    val Schedule = Icons.Outlined.Schedule
    
    // Actions
    val AddToCart = Icons.Outlined.AddShoppingCart
    val RemoveFromCart = Icons.Outlined.RemoveShoppingCart
    val Favorite = Icons.Outlined.Favorite
    val FavoriteFilled = Icons.Filled.Favorite
    val Share = Icons.Outlined.Share
    val Filter = Icons.Outlined.FilterList
    val Sort = Icons.Outlined.Sort
    val Search = Icons.Outlined.Search
    val Scan = Icons.Outlined.QrCodeScanner
    
    // Ratings & Reviews
    val Star = Icons.Filled.Star
    val StarOutline = Icons.Outlined.StarOutline
    val StarHalf = Icons.Outlined.StarHalf
    val Review = Icons.Outlined.RateReview
    
    // Discounts & Offers
    val Discount = Icons.Outlined.LocalOffer
    val Coupon = Icons.Outlined.ConfirmationNumber
    val Sale = Icons.Outlined.Sell
    val Flash = Icons.Outlined.FlashOn
    val New = Icons.Outlined.NewReleases
    val Hot = Icons.Outlined.Whatshot
    
    // Sustainability
    val EcoFriendly = Icons.Outlined.Eco
    val Recycle = Icons.Outlined.Recycling
    val LocalProduce = Icons.Outlined.Agriculture
    val Sustainable = Icons.Outlined.Nature
    val ZeroWaste = Icons.Outlined.DeleteSweep
    
    // User & Profile
    val User = Icons.Outlined.Person
    val Address = Icons.Outlined.Home
    val Payment = Icons.Outlined.CreditCard
    val Settings = Icons.Outlined.Settings
    val Notifications = Icons.Outlined.Notifications
    val Help = Icons.Outlined.HelpOutline
    val Logout = Icons.Outlined.ExitToApp
}

/**
 * Returns the appropriate icon for a food category
 */
fun getFoodCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "vegetables", "veggies" -> FoodIcons.Vegetables
        "fruits", "fruit" -> FoodIcons.Fruits
        "bread", "bakery" -> FoodIcons.Bread
        "dairy", "milk" -> FoodIcons.Dairy
        "meat", "protein" -> FoodIcons.Meat
        "seafood", "fish" -> FoodIcons.Seafood
        "snacks", "chips" -> FoodIcons.Snacks
        "beverages", "drinks" -> FoodIcons.Beverages
        "frozen" -> FoodIcons.Frozen
        "pantry" -> FoodIcons.Pantry
        "organic", "natural" -> FoodIcons.Organic
        "vegan", "plant-based" -> FoodIcons.Vegan
        else -> Icons.Outlined.Category
    }
}

/**
 * Returns the appropriate color for a food category
 */
fun getFoodCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "vegetables", "veggies" -> EcoColors.Green500
        "fruits", "fruit" -> Color(0xFFFF6B6B)
        "bread", "bakery" -> Color(0xFFFFA94D)
        "dairy", "milk" -> Color(0xFF4DABF7)
        "meat", "protein" -> Color(0xFFFA5252)
        "seafood", "fish" -> Color(0xFF339AF0)
        "snacks", "chips" -> Color(0xFFFF922B)
        "beverages", "drinks" -> Color(0xFF5C7CFA)
        "frozen" -> Color(0xFF74C0FC)
        "pantry" -> Color(0xFF8B5CF6)
        "organic", "natural" -> EcoColors.Green600
        "vegan", "plant-based" -> EcoColors.Green500
        else -> Color.Gray
    }
}

/**
 * Returns the appropriate icon for a store type
 */
fun getStoreTypeIcon(storeType: String): ImageVector {
    return when (storeType.lowercase()) {
        "grocery", "supermarket" -> FoodIcons.Grocery
        "restaurant" -> FoodIcons.Restaurant
        "bakery" -> FoodIcons.Bakery
        "cafe", "coffee" -> FoodIcons.Cafe
        "market", "farmers market" -> FoodIcons.Market
        "pharmacy" -> FoodIcons.Pharmacy
        "convenience" -> FoodIcons.Convenience
        else -> Icons.Outlined.Store
    }
}

/**
 * Food placeholder images for different categories.
 * In production, these would be actual image URLs from your backend.
 */
object FoodImages {
    // Category Images
    const val VEGETABLES = "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400"
    const val FRUITS = "https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=400"
    const val BREAD = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400"
    const val DAIRY = "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400"
    const val MEAT = "https://images.unsplash.com/photo-1602470520998-f4a52199a3d6?w=400"
    const val SEAFOOD = "https://images.unsplash.com/photo-1599084993091-1cb5c0721cc6?w=400"
    const val SNACKS = "https://images.unsplash.com/photo-1621939514649-280e2ee25f60?w=400"
    const val BEVERAGES = "https://images.unsplash.com/photo-1544145945-f90425340c7e?w=400"
    
    // Store Images
    const val GROCERY_STORE = "https://images.unsplash.com/photo-1534723452862-4c874018d66d?w=400"
    const val RESTAURANT = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400"
    const val BAKERY = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=400"
    const val CAFE = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400"
    const val FARMERS_MARKET = "https://images.unsplash.com/photo-1488459716781-31db52582fe9?w=400"
    
    // Deal Images
    const val DISCOUNT_BANNER = "https://images.unsplash.com/photo-1607083681678-0975126d2151?w=800"
    const val FLASH_SALE = "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=800"
    const val ECO_FRIENDLY = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=800"
    
    // Placeholder for items without images
    const val PLACEHOLDER = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400"
    
    fun getCategoryImage(category: String): String {
        return when (category.lowercase()) {
            "vegetables", "veggies" -> VEGETABLES
            "fruits", "fruit" -> FRUITS
            "bread", "bakery" -> BREAD
            "dairy", "milk" -> DAIRY
            "meat", "protein" -> MEAT
            "seafood", "fish" -> SEAFOOD
            "snacks", "chips" -> SNACKS
            "beverages", "drinks" -> BEVERAGES
            else -> PLACEHOLDER
        }
    }
    
    fun getStoreImage(storeType: String): String {
        return when (storeType.lowercase()) {
            "grocery", "supermarket" -> GROCERY_STORE
            "restaurant" -> RESTAURANT
            "bakery" -> BAKERY
            "cafe", "coffee" -> CAFE
            "market", "farmers market" -> FARMERS_MARKET
            else -> GROCERY_STORE
        }
    }
}
