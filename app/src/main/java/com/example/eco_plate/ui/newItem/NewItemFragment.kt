package com.example.eco_plate.ui.newItem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.eco_plate.ui.theme.EcoPlateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewItemFragment: Fragment() {
    private val viewModel: NewItemViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                EcoPlateTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        NewItemScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                findNavController().navigateUp()
                            }
                        )
                    }
                }
            }
        }
    }
}