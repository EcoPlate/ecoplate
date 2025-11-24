package com.example.eco_plate

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.eco_plate.databinding.ActivityStoreBinding
import com.example.eco_plate.ui.auth.AuthViewModel
import com.example.eco_plate.ui.auth.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class StoreActivity: AppCompatActivity() {
    private lateinit var binding: ActivityStoreBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observe authentication state
        authViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (!isLoggedIn) {
                // User is not logged in, redirect to login
                navigateToLogin()
            }
        }

        // Hide the action bar for a cleaner look
        supportActionBar?.hide()

        val navView: BottomNavigationView = binding.storeNavView

        val navController = findNavController(R.id.nav_host_fragment_activity_store)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,

            )
        )
        navView.setupWithNavController(navController)

        // Apply modern bottom nav styling
        navView.itemIconTintList = resources.getColorStateList(R.color.bottom_nav_selector, theme)
        navView.itemTextColor = resources.getColorStateList(R.color.bottom_nav_selector, theme)



    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}