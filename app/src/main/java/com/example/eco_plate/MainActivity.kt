package com.example.eco_plate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.eco_plate.databinding.ActivityMainBinding
import com.example.eco_plate.ui.auth.AuthViewModel
import com.example.eco_plate.ui.auth.LoginActivity
import com.example.eco_plate.ui.location.LocationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()
    
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            locationViewModel.onPermissionGranted()
        } else {
            locationViewModel.onPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
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
        
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_search, 
                R.id.navigation_cart,
                R.id.navigation_orders,
                R.id.navigation_profile
            )
        )
        navView.setupWithNavController(navController)
        
        // Apply modern bottom nav styling
        navView.itemIconTintList = resources.getColorStateList(R.color.bottom_nav_selector, theme)
        navView.itemTextColor = resources.getColorStateList(R.color.bottom_nav_selector, theme)
        
        // Check and request location permission
        checkLocationPermission()
    }
    
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                locationViewModel.onPermissionGranted()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show an explanation to the user
                // For now, just request the permission
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                // Request the permission
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}