package com.example.eco_plate.ui.home

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
class HomeFragment : Fragment() {
    
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                            ModernHomeScreen(
                                viewModel = homeViewModel,
                                onNavigateToSearch = {
                                    findNavController().navigate(
                                        com.example.eco_plate.R.id.navigation_search
                                    )
                                },
                                onNavigateToStore = { storeId ->
                                    // Special case: if storeId is "map", navigate to map screen
                                    if (storeId == "map") {
                                        findNavController().navigate(
                                            com.example.eco_plate.R.id.navigation_map
                                        )
                                    } else {
                                        // Navigate to store detail
                                        val bundle = Bundle().apply {
                                            putString("storeId", storeId)
                                        }
                                        try {
                                            findNavController().navigate(
                                                com.example.eco_plate.R.id.navigation_store_detail, bundle
                                            )
                                        } catch (e: Exception) {
                                            // If store detail not found, navigate to map
                                            findNavController().navigate(
                                                com.example.eco_plate.R.id.navigation_map
                                            )
                                        }
                                    }
                                },
                                onNavigateToCategory = { categoryId ->
                                    // TODO: Navigate to category screen
                                },
                                onNavigateToNotifications = {
                                    // Navigate to notifications
                                    findNavController().navigate(
                                        com.example.eco_plate.R.id.navigation_notifications
                                    )
                                },
                    )
                }
            }
        }
    }
}