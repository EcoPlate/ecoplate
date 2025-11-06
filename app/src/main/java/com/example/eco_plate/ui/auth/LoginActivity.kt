package com.example.eco_plate.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.eco_plate.MainActivity
import com.example.eco_plate.databinding.ActivityLoginBinding
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                authViewModel.signIn(email, password)
            }
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is Resource.Error -> {
                    showLoading(false)
                    val errorMessage = when {
                        result.message?.contains("credentials", ignoreCase = true) == true -> 
                            "Invalid email or password"
                        result.message?.contains("network", ignoreCase = true) == true -> 
                            "Network error. Please check your connection"
                        result.message?.contains("deactivated", ignoreCase = true) == true ->
                            "Your account has been deactivated"
                        else -> result.message ?: "Login failed. Please try again"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }

        authViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                navigateToMain()
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            return false
        }
        // Don't validate password format on login, just check if not empty
        
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        return true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.loadingOverlay.isVisible = isLoading
        binding.btnLogin.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
