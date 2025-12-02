package com.example.eco_plate.data.models

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    val email: String,
    val password: String,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val role: String? = null, // "USER" or "STORE_OWNER"
    // Store fields for STORE_OWNER signup
    val storeName: String? = null,
    val storeAddress: String? = null,
    val storePhone: String? = null,
    val storeDescription: String? = null,
    // Store location fields
    val storeCity: String? = null,
    val storeProvince: String? = null,
    val storePostalCode: String? = null,
    val storeLatitude: Double? = null,
    val storeLongitude: Double? = null
)

data class BusinessSignUpRequest(
    val email: String,
    val businessName: String,
    val businessAddress: String,
    val businessPhone: String,
    val businessEmail: String
)

data class SignInRequest(
    val email: String,
    val password: String
)

// The actual auth data that comes inside the wrapper
data class AuthData(
    val user: User?,
    @SerializedName("accessToken")
    val accessToken: String?,
    @SerializedName("refreshToken") 
    val refreshToken: String?
)

// The wrapper response from the backend
data class AuthResponse(
    val success: Boolean?,
    val data: AuthData?,
    val message: String? = null,
    val error: String? = null,
    val timestamp: String? = null,
    val path: String? = null
) {
    // Convenience properties to access nested data
    val user: User? get() = data?.user
    val accessToken: String? get() = data?.accessToken
    val refreshToken: String? get() = data?.refreshToken
}

data class Tokens(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)
