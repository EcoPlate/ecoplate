package com.example.eco_plate.data.models

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    val email: String,
    val password: String,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null
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
