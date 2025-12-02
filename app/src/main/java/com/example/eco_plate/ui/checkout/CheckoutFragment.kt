package com.example.eco_plate.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.eco_plate.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CheckoutFragment : Fragment() {
    
    private val checkoutViewModel: CheckoutViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    CheckoutScreen(
                        viewModel = checkoutViewModel,
                        onBackClick = {
                            findNavController().navigateUp()
                        },
                        onOrderSuccess = { orders ->
                            val bundle = Bundle().apply {
                                putString("orderNumber", orders.firstOrNull()?.orderNumber ?: "")
                                putInt("orderCount", orders.size)
                                putDouble("totalAmount", orders.sumOf { it.total })
                            }
                            findNavController().navigate(R.id.navigation_order_success, bundle)
                        }
                    )
                }
            }
        }
    }
}

