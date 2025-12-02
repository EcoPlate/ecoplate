package com.example.eco_plate.ui.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Co2
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eco_plate.R
import com.example.eco_plate.ui.components.EcoColors
import com.example.eco_plate.ui.profile.EcoImpactSection
import com.example.eco_plate.ui.profile.StatsSection

data class BusinessProfile(
    val name: String,
    val email: String,
    val phone: String,
    val memberSince: String,
    val totalSaved: Float,
    val totalOrders: Int,
    val co2Saved: Float,
    val businessName: String,
    val businessImageUrl: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
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
    val businessProfile by viewModel.businessProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showImageOptionsDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showHelpAndSupportDialog by remember { mutableStateOf(false) }
    var showPaymentMethodsDialog by remember { mutableStateOf(false) }



    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateBusinessImage(uri)
        }
    }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.updateBusinessImage(bitmap)
        }
    }


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
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            businessProfile?.let { profile ->
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
                            headerImageUrl = profile.businessImageUrl,
                            businessName = profile.businessName,
                            ratingText = "4.8 (32)",
                            etaText = "10–15 min",
                            deliveryText = "Free",
                            onEditImage = { showImageOptionsDialog = true},
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
                            onNavigateToPayments = {showPaymentMethodsDialog = true},
                            onNavigateToNotifications = onNavigateToNotifications,
                            onNavigateToLanguage = { showLanguageDialog = true },
                            onNavigateToPrivacy = onNavigateToPrivacy,
                            onNavigateToSupport = { showHelpAndSupportDialog = true },
                            onNavigateToAbout = onNavigateToAbout,
                            onSignOut = { showSignOutDialog = true }
                        )
                    }

                    //Eco Impact Section
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
            currentEmail = businessProfile?.email ?: "",
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

    if (showImageOptionsDialog) {
        EditImageChoiceDialog(
            onDismiss = { showImageOptionsDialog = false },
            onChooseGallery = {
                showImageOptionsDialog = false
                // TODO: launch gallery picker here
            },
            onChooseCamera = {
                showImageOptionsDialog = false
                // TODO: launch camera intent here
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

    val methodsTest = listOf(
        PaymentMethod("1", "Visa •••• 4242", "Expires 08/27"),
        PaymentMethod("2", "Mastercard •••• 1111", "Expires 01/26")
    )

    if (showPaymentMethodsDialog) {
        PaymentMethodsDialog(
            methodsTest,
            { showPaymentMethodsDialog = false },
            { showPaymentMethodsDialog = false },
            {id, title ->
                // handle selected payment method

                showPaymentMethodsDialog = false
            }
        )
    }

}

@Composable
private fun ProfileHeader(
    profile: BusinessProfile,
    headerImageUrl: String? = null,
    businessName: String = profile.name,
    ratingText: String? = null,
    etaText: String? = null,
    deliveryText: String? = null,
    onEditImage: () -> Unit = {},
    onEditEmail: () -> Unit = {},
    onEditPassword: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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



            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (!headerImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = headerImageUrl,
                        contentDescription = "$businessName header",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                IconButton(
                    onClick = onEditImage,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit header image"
                    )
                }

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                )
            }

            // --- 2) Business info section ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = businessName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // optional quick stats row like screenshot
                if (ratingText != null || etaText != null || deliveryText != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ratingText?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFFFC107)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        etaText?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        deliveryText?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
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
}

@Composable
private fun StatsSection(profile: BusinessProfile) {
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
                    onClick = onNavigateToSupport
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
private fun EcoImpactSection(profile: BusinessProfile) {
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
                text = "By selling rescued food, you've made a real difference:",
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











//////////////////////////////////////////////////////////////////
//                          Previews                            //
//////////////////////////////////////////////////////////////////

private val previewBusinessProfile = BusinessProfile(
    name = "Business Name",
    email = "bakery@example.com",
    phone = "604-123-4567",
    memberSince = "Member since 2021",
    totalSaved = 150.0f,
    totalOrders = 42,
    co2Saved = 56.3f,
    businessName = "Daniel’s Bakery",
    businessImageUrl = "https://picsum.photos/800/400"
)

@Preview(showBackground = true)
@Composable
private fun ProfileHeaderPreview() {
    ProfileHeader(
        profile = previewBusinessProfile,
        ratingText = "4.8 (32)",
        etaText = "10–15 min",
        deliveryText = "Free"
    )
}

@Preview(showBackground = true)
@Composable
private fun StatsSectionPreview() {
    StatsSection(profile = previewBusinessProfile)
}

@Preview(showBackground = true)
@Composable
private fun EcoImpactPreview() {
    EcoImpactSection(profile = previewBusinessProfile)
}


@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun BusinessProfileScreenPreview() {
    BusinessProfileScreenContent(profile = previewBusinessProfile)
}


@Composable
private fun BusinessProfileScreenContent(
    profile: BusinessProfile?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProfileHeader(
                profile = profile!!,
                ratingText = "4.8 (32)",
                etaText = "10–15 min",
                deliveryText = "Free"
            )
        }
        item { StatsSection(profile!!) }
        item { QuickActionsSection({}, {}) }
        item { SettingsSection({}, {}, {}, {}, {},  {}, {}) }
        item { EcoImpactSection(profile!!) }
    }
}
