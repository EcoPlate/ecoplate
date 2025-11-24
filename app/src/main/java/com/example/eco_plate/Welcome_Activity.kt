package com.example.eco_plate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.eco_plate.databinding.ActivityWelcomeBinding
import com.example.eco_plate.ui.auth.AuthViewModel
import com.example.eco_plate.ui.auth.LoginActivity
import com.example.eco_plate.ui.auth.UserTypeSelectionActivity
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

        checkNotificationPermission()
        observeAuthState()
        setupClickListeners()
    }

    private fun observeAuthState() {
        // Observe auth check completion
        authViewModel.authCheckComplete.observe(this) { isComplete ->
            if (isComplete && !hasNavigated) {
                handleNavigation()
            }
        }
        
        // Also observe role changes to handle any updates
        authViewModel.userRole.observe(this) { role ->
            val isComplete = authViewModel.authCheckComplete.value ?: false
            val isLoggedIn = authViewModel.isLoggedIn.value ?: false
            
            // If auth is complete, logged in, and role changed, re-evaluate navigation
            if (isComplete && isLoggedIn && !hasNavigated && role != null) {
                handleNavigation()
                    }
                }
            }
    
    private fun handleNavigation() {
        if (hasNavigated) return
        
        val isLoggedIn = authViewModel.isLoggedIn.value ?: false
        val userRole = authViewModel.userRole.value
        
        if (isLoggedIn) {
                hasNavigated = true
            // Navigate based on user role
            Log.d("Welcome_Activity", "Navigating for user role: $userRole")
                when (userRole) {
                "STORE_OWNER" -> {
                    Log.d("Welcome_Activity", "Navigating to Store Activity")
                    navigateToStoreActivity()
                }
                "USER", null -> {
                    Log.d("Welcome_Activity", "Navigating to Main Activity")
                    navigateToMainActivity()
                }
                else -> {
                    Log.d("Welcome_Activity", "Navigating to Main Activity for unknown role: $userRole")
                    navigateToMainActivity()
                }
            }
        } else {
            // User is not logged in, show the welcome UI
            showWelcomeUI()
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
        val intent = Intent(this, UserTypeSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }

    private fun navigateToStoreActivity() {
        val intent = Intent(this, StoreActivity::class.java)
        startActivity(intent)
        finish()
    }
}
