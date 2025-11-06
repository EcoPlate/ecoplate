package com.example.eco_plate.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.eco_plate.MainActivity
import com.example.eco_plate.databinding.ActivitySignUpBinding
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (validateInput(firstName, lastName, email, password, confirmPassword, username)) {
                authViewModel.signUp(
                    email = email, 
                    password = password, 
                    firstName = firstName.ifEmpty { null }, 
                    lastName = lastName.ifEmpty { null },
                    username = username.ifEmpty { null },
                    phone = phone.ifEmpty { null }
                )
            }
        }

        binding.tvLogin.setOnClickListener {
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
                    Toast.makeText(this, "Account created successfully! Welcome!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is Resource.Error -> {
                    showLoading(false)
                    val errorMessage = when {
                        result.message?.contains("already exists", ignoreCase = true) == true -> 
                            "This email or username is already taken"
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
        username: String
    ): Boolean {
        var isValid = true
        
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
        
        // Password validation - must match backend requirements
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 8 || password.length > 32) {
            binding.tilPassword.error = "Password must be 8-32 characters"
            isValid = false
        } else if (!password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&].*$"))) {
            binding.tilPassword.error = "Password must contain uppercase, lowercase, number, and special character"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        
        // Confirm password
        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }
        
        // Username validation (optional)
        if (username.isNotEmpty()) {
            if (username.length < 3 || username.length > 30) {
                binding.tilUsername.error = "Username must be 3-30 characters"
                isValid = false
            } else if (!username.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
                binding.tilUsername.error = "Username can only contain letters, numbers, underscores, and hyphens"
                isValid = false
            } else {
                binding.tilUsername.error = null
            }
        } else {
            binding.tilUsername.error = null
        }
        
        // Clear other field errors
        binding.tilFirstName.error = null
        binding.tilLastName.error = null
        binding.tilPhone.error = null
        
        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.loadingOverlay.isVisible = isLoading
        binding.btnSignUp.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
