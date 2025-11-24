package com.example.eco_plate.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.MainActivity
import com.example.eco_plate.databinding.ActivityBusinessSignUpBinding
import com.example.eco_plate.ui.profile.UserProfile
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class BusinessSignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessSignUpBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusinessSignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupClickListeners()
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
                    navigateToBusinessMain()
                }
                is Resource.Error -> {
                    showLoading(false)
                    val errorMessage = when {
                        result.message?.contains("network", ignoreCase = true) == true ->
                            "Network error. Please check your connection"
                        else -> result.message ?: "Sign up failed. Please try again"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val businessName = binding.etBusinessName.text.toString().trim()
            val businessAddress = binding.etBusinessAdress.text.toString().trim()
            val businessPhone = binding.etBusinessPhone.text.toString().trim()
            val businessEmail = binding.etBusinessEmail.text.toString().trim()

            if (validateInput(businessName, businessAddress, businessPhone, businessEmail)) {
                val currentUser = authViewModel.getCurrentUser()
                val userEmail = currentUser?.email

                authViewModel.businessSignUp(
                    userEmail = userEmail!!,
                    businessName = businessName,
                    businessAddress = businessAddress,
                    businessPhone = businessPhone,
                    businessEmail = businessEmail
                )
            }
        }
    }

    private fun validateInput(
        businessName: String,
        businessAddress: String,
        businessPhone: String,
        businessEmail: String
    ): Boolean {
        var isValid = true

        // Business Name
        if (businessName.isEmpty()) {
            binding.tilBusinessName.error = "Business Name is required"
            isValid = false
        } else {
            binding.tilBusinessEmail.error = null
        }

        // Business Address
        if (businessAddress.isEmpty()) {
            binding.tilBusinessAdress.error = "Business Address is required"
            isValid = false
        }

        // Business Phone
        if (businessAddress.isEmpty()) {
            binding.tilBusinessPhone.error = "Business Phone is required"
            isValid = false
        } else if (!android.util.Patterns.PHONE.matcher(businessPhone).matches()) {
            binding.tilBusinessPhone.error = "Invalid Phone format"
            isValid = false
        } else {
            binding.tilBusinessPhone.error = null
        }

        // Email validation
        if (businessEmail.isEmpty()) {
            binding.tilBusinessEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(businessEmail).matches()) {
            binding.tilBusinessEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.tilBusinessEmail.error = null
        }

        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.loadingOverlay.isVisible = isLoading
        binding.btnSignUp.isEnabled = !isLoading
    }

    private fun navigateToBusinessMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("isBusinessUser", true)
        startActivity(intent)
        finish()
    }
}