package com.example.eco_plate.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Open Food Facts API for looking up product data from barcodes
 * https://world.openfoodfacts.org/api/v0/product/{barcode}.json
 */
interface BarcodeApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): Response<OpenFoodFactsResponse>
}

data class OpenFoodFactsResponse(
    val status: Int, // 1 = found, 0 = not found
    val status_verbose: String?,
    val product: OpenFoodFactsProduct?
)

data class OpenFoodFactsProduct(
    val code: String?, // barcode
    val product_name: String?,
    val brands: String?,
    val generic_name: String?,
    val categories: String?,
    val quantity: String?,
    val image_url: String?,
    val image_front_url: String?,
    val image_front_small_url: String?,
    val nutriments: OpenFoodFactsNutriments?,
    val allergens: String?,
    val allergens_tags: List<String>?,
    val labels: String?,
    val labels_tags: List<String>?,
    val ingredients_text: String?
)

data class OpenFoodFactsNutriments(
    val energy_kcal_100g: Double?,
    val proteins_100g: Double?,
    val carbohydrates_100g: Double?,
    val fat_100g: Double?,
    val fiber_100g: Double?,
    val sugars_100g: Double?,
    val sodium_100g: Double?,
    val salt_100g: Double?
)

