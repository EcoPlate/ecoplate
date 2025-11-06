package com.example.eco_plate.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eco_plate.ui.theme.EcoPlateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Get orderId from arguments
        val orderId = arguments?.getString("orderId") ?: "12345"
        
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                EcoPlateTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        OrderDetailsScreen(
                            orderId = orderId,
                            onBackClick = {
                                findNavController().navigateUp()
                            },
                            onCallDriver = {
                                // TODO: Implement call functionality
                            },
                            onMessageDriver = {
                                // TODO: Implement message functionality
                            },
                            onReportIssue = {
                                // TODO: Navigate to support/help
                            }
                        )
                    }
                }
            }
        }
    }
}
