package com.example.eco_plate.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.eco_plate.R
import com.example.eco_plate.ui.home.HomeViewModel
import com.example.eco_plate.ui.theme.EcoPlateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment() {

    private val mapViewModel: MapViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                EcoPlateTheme {
                    // Get delivery address coordinates from HomeViewModel
                    val deliveryCoords = homeViewModel.getCurrentDeliveryLocation()
                    
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SafeGoogleMapScreen(
                            deliveryLatitude = deliveryCoords?.first,
                            deliveryLongitude = deliveryCoords?.second,
                            onBackClick = {
                                findNavController().navigateUp()
                            },
                            onStoreClick = { storeId ->
                                val bundle = Bundle().apply {
                                    putString("storeId", storeId)
                                }
                                findNavController().navigate(R.id.navigation_store_detail, bundle)
                            }
                        )
                    }
                }
            }
        }
    }
}
