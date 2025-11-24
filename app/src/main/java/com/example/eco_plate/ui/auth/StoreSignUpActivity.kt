package com.example.eco_plate.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.eco_plate.MainActivity
import com.example.eco_plate.StoreActivity
import com.example.eco_plate.databinding.ActivityStoreSignUpBinding
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreSignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoreSignUpBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoreSignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            // Personal Information
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            
            // Store Information
            val storeName = binding.etStoreName.text.toString().trim()
            val storeAddress = binding.etStoreAddress.text.toString().trim()
            val storePhone = binding.etStorePhone.text.toString().trim()
            val storeDescription = binding.etStoreDescription.text.toString().trim()

            if (validateInput(firstName, lastName, email, password, confirmPassword, storeName, storeAddress, storePhone)) {
                // First create the user account with STORE_OWNER role
                authViewModel.signUpAsStoreOwner(
                    email = email,
                    password = password,
                    firstName = firstName.ifEmpty { null },
                    lastName = lastName.ifEmpty { null },
                    phone = phone.ifEmpty { null },
                    storeName = storeName,
                    storeAddress = storeAddress,
                    storePhone = storePhone,
                    storeDescription = storeDescription.ifEmpty { null }
                )
            }
        }

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        binding.tvBackToSelection.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        authViewModel.authState.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Store account created successfully!", Toast.LENGTH_SHORT).show()
                    navigateToStoreMain()
                }
                is Resource.Error -> {
                    showLoading(false)
                    val errorMessage = when {
                        result.message?.contains("already exists", ignoreCase = true) == true -> 
                            "This email is already registered"
                        result.message?.contains("network", ignoreCase = true) == true -> 
                            "Network error. Please check your connection"
                        result.message?.contains("password", ignoreCase = true) == true ->
                            "Password requirements not met"
                        else -> result.message ?: "Sign up failed. Please try again"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInput(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        storeName: String,
        storeAddress: String,
        storePhone: String
    ): Boolean {
        var isValid = true
        
        // Personal Information Validation
        if (firstName.isEmpty()) {
            binding.tilFirstName.error = "First name is required"
            isValid = false
        } else {
            binding.tilFirstName.error = null
        }
        
        if (lastName.isEmpty()) {
            binding.tilLastName.error = "Last name is required"
            isValid = false
        } else {
            binding.tilLastName.error = null
        }
        
        // Email validation
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        
        // Password validation
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            binding.tilPassword.error = "Password must be at least 8 characters"
            isValid = false
        } else if (!password.matches(Regex(".*[A-Z].*"))) {
            binding.tilPassword.error = "Password must contain at least one uppercase letter"
            isValid = false
        } else if (!password.matches(Regex(".*[a-z].*"))) {
            binding.tilPassword.error = "Password must contain at least one lowercase letter"
            isValid = false
        } else if (!password.matches(Regex(".*[0-9].*"))) {
            binding.tilPassword.error = "Password must contain at least one number"
            isValid = false
        } else if (!password.matches(Regex(".*[@\$!%*?&].*"))) {
            binding.tilPassword.error = "Password must contain at least one special character"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        
        // Confirm password
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (confirmPassword != password) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }
        
        // Store Information Validation
        if (storeName.isEmpty()) {
            binding.tilStoreName.error = "Store name is required"
            isValid = false
        } else {
            binding.tilStoreName.error = null
        }
        
        if (storeAddress.isEmpty()) {
            binding.tilStoreAddress.error = "Store address is required"
            isValid = false
        } else {
            binding.tilStoreAddress.error = null
        }
        
        if (storePhone.isEmpty()) {
            binding.tilStorePhone.error = "Store phone is required"
            isValid = false
        } else if (!android.util.Patterns.PHONE.matcher(storePhone).matches()) {
            binding.tilStorePhone.error = "Invalid phone format"
            isValid = false
        } else {
            binding.tilStorePhone.error = null
        }
        
        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.loadingOverlay.isVisible = isLoading
        binding.btnSignUp.isEnabled = !isLoading
    }

    private fun navigateToStoreMain() {
        val intent = Intent(this, StoreActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
