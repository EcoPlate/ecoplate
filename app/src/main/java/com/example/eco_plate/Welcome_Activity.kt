package com.example.eco_plate

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.eco_plate.databinding.ActivityWelcomeBinding
import com.example.eco_plate.ui.auth.AuthViewModel
import com.example.eco_plate.ui.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeAuthState()
        setupClickListeners()
    }

    private fun observeAuthState() {
        // First, observe when the auth check is complete
        authViewModel.authCheckComplete.observe(this) { isComplete ->
            if (isComplete && !hasNavigated) {
                // Auth check is done, now check if user is logged in
                authViewModel.isLoggedIn.value?.let { isLoggedIn ->
                    if (isLoggedIn) {
                        hasNavigated = true
                        navigateToMainActivity()
                    } else {
                        // User is not logged in, show the welcome UI
                        showWelcomeUI()
                    }
                }
            }
        }

        // Also observe login state changes for real-time updates
        authViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            val authCheckComplete = authViewModel.authCheckComplete.value ?: false
            if (authCheckComplete && isLoggedIn && !hasNavigated) {
                hasNavigated = true
                navigateToMainActivity()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            if (!hasNavigated) {
                hasNavigated = true
                navigateToLogin()
            }
        }
    }

    private fun showWelcomeUI() {
        // Show the welcome UI elements (they should be hidden by default)
        binding.btnGetStarted.isVisible = true
        binding.progressBar.isVisible = false
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
