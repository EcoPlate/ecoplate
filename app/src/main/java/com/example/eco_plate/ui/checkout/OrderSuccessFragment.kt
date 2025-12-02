package com.example.eco_plate.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eco_plate.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderSuccessFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent going back to checkout
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateToHome()
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val orderNumber = arguments?.getString("orderNumber") ?: ""
        val orderCount = arguments?.getInt("orderCount") ?: 1
        val totalAmount = arguments?.getDouble("totalAmount") ?: 0.0

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    OrderSuccessScreen(
                        orderNumber = orderNumber,
                        orderCount = orderCount,
                        totalAmount = totalAmount,
                        onViewOrders = {
                            findNavController().navigate(R.id.navigation_orders)
                        },
                        onContinueShopping = {
                            navigateToHome()
                        }
                    )
                }
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.navigation_home) {
            popUpTo(R.id.navigation_home) { inclusive = true }
        }
    }
}

