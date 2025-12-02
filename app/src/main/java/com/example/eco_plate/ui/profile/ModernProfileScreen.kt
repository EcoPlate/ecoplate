package com.example.eco_plate.ui.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.example.eco_plate.ui.components.EcoColors
import androidx.compose.runtime.collectAsState
import com.example.eco_plate.R
import com.example.eco_plate.utils.Resource

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val memberSince: String,
    val totalSaved: Float,
    val totalOrders: Int,
    val co2Saved: Float,
    val profilePicture: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToOrders: () -> Unit = {},
    onNavigateToAddresses: () -> Unit = {},
    onNavigateToPayments: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showHelpAndSupportDialog by remember { mutableStateOf(false) }


    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            userProfile?.let { profile ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Header
                    item {
                        ProfileHeader(
                            profile = profile,
                            onEditEmail = { showEmailDialog = true },
                            onEditPassword = { showPasswordDialog = true }
                        )
                    }
                    
                    // Stats Cards
                    item {
                        StatsSection(profile)
                    }
                    
                    // Quick Actions
                    item {
                        QuickActionsSection(
                            onNavigateToOrders = onNavigateToOrders,
                            onNavigateToAddresses = onNavigateToAddresses
                        )
                    }
                    
                    // Settings Section
                    item {
                        SettingsSection(
                            onNavigateToPayments = onNavigateToPayments,
                            onNavigateToNotifications = onNavigateToNotifications,
                            onNavigateToLanguage = { showLanguageDialog = true } ,
                            onNavigateToPrivacy = onNavigateToPrivacy,
                            onNavigateToSupport = onNavigateToSupport,
                            onNavigateToHelpAndSupport = { showHelpAndSupportDialog = true },
                            onNavigateToAbout = onNavigateToAbout,
                            onSignOut = { showSignOutDialog = true }
                        )
                    }
                    
                    // Eco Impact Section
                    item {
                        EcoImpactSection(profile)
                    }
                    
                    // App Version
                    item {
                        Text(
                            text = "Version 1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } ?: run {
                // Show sign in prompt if no user data
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Please sign in to view your profile",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onSignOut) {
                            Text("Sign In")
                        }
                    }
                }
            }
        }
    }
    
    // Sign Out Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Sign Out?") },
            text = { Text("Are you sure you want to sign out of your account?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    }
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Email Edit Dialog
    if (showEmailDialog) {
        EditEmailDialog(
            currentEmail = userProfile?.email ?: "",
            onDismiss = { showEmailDialog = false },
            onConfirm = { newEmail ->
                viewModel.updateEmail(newEmail) { success, message ->
                    if (success) {
                        // Show success message
                    } else {
                        // Show error message
                    }
                }
                showEmailDialog = false
            }
        )
    }
    
    // Password Edit Dialog
    if (showPasswordDialog) {
        EditPasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { oldPassword, newPassword ->
                viewModel.changePassword(oldPassword, newPassword) { success, message ->
                    if (success) {
                        // Show success message
                    } else {
                        // Show error message
                    }
                }
                showPasswordDialog = false
            }
        )
    }

    val languageOptions = listOf(
        LanguageOption("en", "English", R.drawable.outline_language_us_24)

    )

    if (showLanguageDialog) {
        EditLanguageDialog(
            options = languageOptions,
            initialSelectionCode = "en",
            onDismiss = { showLanguageDialog = false },
            onConfirm = { code, name ->
                showLanguageDialog = false
            }
        )
    }

    if (showHelpAndSupportDialog) {
        HelpAndSupportDialog(
            onDismiss = { showHelpAndSupportDialog = false }
        )
    }
}

@Composable
private fun ProfileHeader(
    profile: UserProfile,
    onEditEmail: () -> Unit = {},
    onEditPassword: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        EcoColors.Green100
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.name.split(" ").map { it.first() }.joinToString(""),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = EcoColors.Green600
                )
            }
            
            // Name and Email
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = profile.memberSince,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Edit Profile Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditEmail,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Email")
                }
                
                OutlinedButton(
                    onClick = onEditPassword,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Password")
                }
            }
        }
    }
}

@Composable
private fun StatsSection(profile: UserProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.ShoppingBag,
            value = profile.totalOrders.toString(),
            label = "Total Orders",
            color = MaterialTheme.colorScheme.primary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.Savings,
            value = "$${profile.totalSaved}",
            label = "Total Saved",
            color = EcoColors.Green500
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.Eco,
            value = "${profile.co2Saved} kg",
            label = "CO2 Saved",
            color = Color(0xFF4CAF50)
        )
    }
}


@Composable
private fun QuickActionsSection(
    onNavigateToOrders: () -> Unit,
    onNavigateToAddresses: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Receipt,
                title = "Order History",
                subtitle = "View all orders",
                onClick = onNavigateToOrders

            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.LocationOn,
                title = "Addresses",
                subtitle = "Manage locations",
                onClick = onNavigateToAddresses
            )
        }
    }
}

@Composable
private fun SettingsSection(
    onNavigateToPayments: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToHelpAndSupport: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Outlined.CreditCard,
                    title = "Payment Methods",
                    onClick = onNavigateToPayments
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Notifications",
                    onClick = onNavigateToNotifications
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Outlined.Language,
                    title = "Language",
                    subtitle = "English",
                    onClick = onNavigateToLanguage
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Outlined.Security,
                    title = "Privacy & Security",
                    onClick = onNavigateToPrivacy
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Help & Support",
                    onClick = onNavigateToHelpAndSupport
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "About",
                    onClick = onNavigateToAbout
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Outlined.Logout,
                    title = "Sign Out",
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = onSignOut
                )
            }
        }
    }
}

@Composable
private fun EcoImpactSection(profile: UserProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = EcoColors.Green50
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Eco,
                    contentDescription = null,
                    tint = EcoColors.Green600,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Your Eco Impact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EcoColors.Green800
                )
            }
            
            Text(
                text = "By choosing rescued food, you've made a real difference:",
                style = MaterialTheme.typography.bodyMedium,
                color = EcoColors.Green700
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImpactItem(
                    icon = Icons.Outlined.Co2,
                    text = "${profile.co2Saved} kg of CO2 prevented",
                    equivalent = "Equivalent to driving ${(profile.co2Saved * 6).toInt()} km less"
                )
                ImpactItem(
                    icon = Icons.Outlined.Restaurant,
                    text = "${(profile.totalOrders * 2.5).toInt()} meals saved",
                    equivalent = "That's ${(profile.totalOrders * 2.5 / 7).toInt()} weeks of food"
                )
                ImpactItem(
                    icon = Icons.Outlined.Water,
                    text = "${(profile.co2Saved * 100).toInt()} liters of water saved",
                    equivalent = "Enough for ${(profile.co2Saved * 100 / 150).toInt()} showers"
                )
            }
            
            LinearProgressIndicator(
                progress = 0.7f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = EcoColors.Green500,
                trackColor = EcoColors.Green200
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "70% to Gold Status",
                    style = MaterialTheme.typography.bodySmall,
                    color = EcoColors.Green600
                )
                Text(
                    text = "30 more orders",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = EcoColors.Green600
                )
            }
        }
    }
}
