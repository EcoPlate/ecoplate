package com.example.eco_plate.data

class TestAuthorizationRepo(private val apiKey: Int = 1): AuthorizationRepository {

    override fun login(
        email: String,
        password: String
    ): LoginResult {
        return if (email == "test@test.com" && password == "test") {
            LoginResult(true, "Login successful")
        } else {
            LoginResult(false, "Invalid email or password")
        }
    }

    override fun signup(
        email: String,
        password: String,
        accountType: String
    ): LoginResult {
        if (email.isBlank() || password.isBlank()) {
            return LoginResult(false, "Missing email or password")
        }
        return LoginResult(true, "Account created successfully ($accountType)")
    }

}