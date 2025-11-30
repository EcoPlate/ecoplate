package com.example.eco_plate.ui.profile

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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.eco_plate.R
import com.example.eco_plate.ui.theme.EcoPlateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BusinessProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private var hasInitiallyLoaded = false

    override fun onResume() {
        super.onResume()
        // Only refresh if we're actually visible to avoid interfering with navigation
        if (isVisible && !isStateSaved) {
            // Only refresh if we haven't loaded initially or if we're returning after being away
            if (!hasInitiallyLoaded) {
                viewModel.refreshProfile()
                hasInitiallyLoaded = true
            }
        }
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
                        BusinessProfileScreen(
                            viewModel = viewModel,
                            onNavigateToOrders = {
                                // Navigate immediately without waiting for any async operations
                                view?.post {
                                    findNavController().navigate(R.id.navigation_orders)
                                }
                            },
                            onNavigateToAddresses = {
                                // TODO: Navigate to addresses screen
                            },
                            onNavigateToPayments = {
                                // TODO: Navigate to payments screen
                            },
                            onNavigateToNotifications = {
                                // Navigate immediately without waiting for any async operations
                                view?.post {
                                    findNavController().navigate(R.id.navigation_notifications)
                                }
                            },
                            onNavigateToSupport = {
                                // TODO: Navigate to support screen
                            },
                            onNavigateToAbout = {
                                // TODO: Navigate to about screen
                            },
                            onSignOut = {
                                // Sign out and navigate to login screen
                                viewModel.signOut()
                                val intent = android.content.Intent(requireContext(), com.example.eco_plate.ui.auth.LoginActivity::class.java)
                                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}