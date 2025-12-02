package com.example.eco_plate.ui.storeHome

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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.eco_plate.ui.theme.EcoPlateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreHomeFragment : Fragment(){
    // Use activityViewModels to persist data across tab navigation
    private val viewModel: StoreHomeViewModel by activityViewModels()
    private var hasNavigatedAway = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                EcoPlateTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val storeId = arguments?.getString("storeId") ?: ""
                        StoreHomeScreen(
                            storeId = storeId,
                            viewModel = viewModel,
                            onBackClick = {
                                findNavController().navigateUp()
                            },
                            onAddNewItem = {
                                hasNavigatedAway = true
                                findNavController().navigate(com.example.eco_plate.R.id.navigation_new_item)
                            },
                            showBackButton = false // This is the store owner's home screen
                        )
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh when coming back from adding a new item
        if (hasNavigatedAway) {
            hasNavigatedAway = false
            viewModel.refreshStore()
        }
    }
}