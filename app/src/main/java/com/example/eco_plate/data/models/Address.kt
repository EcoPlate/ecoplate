package com.example.eco_plate.data.models

import com.google.gson.annotations.SerializedName

data class UserAddress(
    val id: String,
    val userId: String,
    val label: String,
    val line1: String,
    val line2: String? = null,
    val city: String,
    val region: String,
    val postalCode: String,
    val country: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val placeId: String? = null,
    val isDefault: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val fullAddress: String
        get() = buildString {
            append(line1)
            line2?.let { append(", $it") }
            append(", $city, $region $postalCode")
        }
    
    val shortAddress: String
        get() = "$line1, $city"
}

data class CreateAddressRequest(
    val label: String,
    val line1: String,
    val line2: String? = null,
    val city: String,
    val region: String,
    val postalCode: String,
    val country: String = "Canada",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val placeId: String? = null,
    val isDefault: Boolean? = null
)

data class UpdateAddressRequest(
    val label: String? = null,
    val line1: String? = null,
    val line2: String? = null,
    val city: String? = null,
    val region: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val placeId: String? = null,
    val isDefault: Boolean? = null
)

data class AddressResponse(
    val success: Boolean,
    val data: UserAddress?,
    val message: String? = null
)

data class AddressListResponse(
    val success: Boolean,
    val data: List<UserAddress>,
    val message: String? = null
)

