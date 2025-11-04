package com.example.eco_plate.data

data class LoginResult(
    val success: Boolean,
    val message: String? = null
)

interface AuthorizationRepository {
    fun login(email: String, password: String): LoginResult
    fun signup(email: String, password: String, accountType: String): LoginResult
}