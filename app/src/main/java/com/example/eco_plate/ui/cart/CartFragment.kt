package com.example.eco_plate.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : Fragment() {
    
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ModernCartScreen(
                        viewModel = cartViewModel,
                        onNavigateToCheckout = {
                            // TODO: Navigate to checkout screen
                        },
                        onNavigateBack = {
                            findNavController().navigateUp()
                        },
                        onNavigateToHome = {
                            // Navigate to home page (main page with categories and stores)
                            findNavController().navigate(com.example.eco_plate.R.id.navigation_home)
                        },
                        onNavigateToStore = { storeId ->
                            // Navigate to store detail
                            val bundle = Bundle().apply {
                                putString("storeId", storeId)
                            }
                            try {
                                findNavController().navigate(
                                    com.example.eco_plate.R.id.navigation_store_detail,
                                    bundle
                                )
                            } catch (e: Exception) {
                                // If store detail not found, navigate to home
                                findNavController().navigate(com.example.eco_plate.R.id.navigation_home)
                            }
                        }
                    )
                }
            }
        }
    }
}
