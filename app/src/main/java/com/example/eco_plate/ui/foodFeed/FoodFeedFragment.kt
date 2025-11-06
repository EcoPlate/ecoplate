package com.example.eco_plate.ui.foodFeed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.eco_plate.R
import com.example.eco_plate.ui.search.ModernSearchScreen
import com.example.eco_plate.ui.search.SearchViewModel
import com.example.eco_plate.ui.theme.EcoPlateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FoodFeedFragment : Fragment() {

    private val searchViewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                EcoPlateTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Using the modern search screen
                        ModernSearchScreen(
                            viewModel = searchViewModel,
                            onBackClick = {
                                findNavController().navigateUp()
                            },
                            onProductClick = { productId ->
                                // TODO: Navigate to product detail
                            },
                            onStoreClick = { storeId ->
                                // TODO: Navigate to store detail
                            },
                            onNavigateToCart = {
                                findNavController().navigate(R.id.navigation_cart)
                            }
                        )
                    }
                }
            }
        }
    }
}
