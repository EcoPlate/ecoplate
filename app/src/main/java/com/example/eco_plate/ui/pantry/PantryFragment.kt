package com.example.eco_plate.ui.pantry

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.eco_plate.R
import com.example.eco_plate.ui.profile.ModernProfileScreen
import com.example.eco_plate.ui.profile.ProfileViewModel
import com.example.eco_plate.ui.theme.EcoPlateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PantryFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        // Refresh profile data when screen is displayed
        profileViewModel.refreshProfile()
    }

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
                        // Using the modern profile screen
                        ModernProfileScreen(
                            viewModel = profileViewModel,
                            onNavigateToOrders = {
                                findNavController().navigate(R.id.navigation_orders)
                            },
                            onNavigateToAddresses = {
                                // TODO: Navigate to addresses screen
                            },
                            onNavigateToPayments = {
                                // TODO: Navigate to payments screen
                            },
                            onNavigateToNotifications = {
                                findNavController().navigate(R.id.navigation_notifications)
                            },
                            onNavigateToSupport = {
                                // TODO: Navigate to support screen
                            },
                            onNavigateToAbout = {
                                // TODO: Navigate to about screen
                            },
                            onSignOut = {
                                profileViewModel.signOut()
                                // Navigate to login screen
                                val intent = Intent(requireContext(), com.example.eco_plate.ui.auth.LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        )
                    }
                }
            }
        }
    }
}