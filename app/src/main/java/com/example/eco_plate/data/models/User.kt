package com.example.eco_plate.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class User(
    val id: String,
    val email: String,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    @SerializedName("role")
    val role: String? = "USER",  // Backend returns string, not enum
    val isActive: Boolean = true,
    val profilePicture: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) : Parcelable {
    // Helper function to get role as enum
    fun getRoleAsEnum(): UserRole {
        return try {
            UserRole.valueOf(role?.uppercase() ?: "USER")
        } catch (e: IllegalArgumentException) {
            UserRole.USER
        }
    }
}

@Parcelize
data class BusinessUser(
    val id: String,
    val email: String,
    val businessName : String? = null,
    val businessAddress: String? = null,
    val businessImageUrl: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    @SerializedName("role")
    val role: String? = "STORE_OWNER",  // Backend returns string, not enum
    val isActive: Boolean = true,
    val profilePicture: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) : Parcelable {
    // Helper function to get role as enum
    fun getRoleAsEnum(): UserRole {
        return try {
            UserRole.valueOf(role?.uppercase() ?: "STORE_OWNER")
        } catch (e: IllegalArgumentException) {
            UserRole.STORE_OWNER
        }
    }
}

enum class UserRole {
    USER,
    STORE_OWNER,
    ADMIN
}

@Parcelize
data class Address(
    val id: String,
    val userId: String,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val isDefault: Boolean,
    @SerializedName("created_at")
    val createdAt: String
) : Parcelable
