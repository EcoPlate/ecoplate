package com.example.eco_plate.ui.profile

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.example.eco_plate.ui.components.EcoColors
import androidx.compose.runtime.collectAsState

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
    onNavigateToSupport: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    
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
                            onNavigateToSupport = onNavigateToSupport,
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
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
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
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSection(
    onNavigateToPayments: () -> Unit,
    onNavigateToNotifications: () -> Unit,
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
                    onClick = { }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Outlined.Security,
                    title = "Privacy & Security",
                    onClick = { }
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
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.7f)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

@Composable
private fun ImpactItem(
    icon: ImageVector,
    text: String,
    equivalent: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = EcoColors.Green600,
            modifier = Modifier.size(20.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = EcoColors.Green800
            )
            Text(
                text = equivalent,
                style = MaterialTheme.typography.bodySmall,
                color = EcoColors.Green600
            )
        }
    }
}

@Composable
private fun EditEmailDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var email by remember { mutableStateOf(currentEmail) }
    var isError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Change Email") },
        text = {
            Column {
                Text(
                    "Enter your new email address",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        isError = false
                    },
                    label = { Text("Email") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = "Please enter a valid email",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        onConfirm(email)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showOldPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Change Password") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { 
                        oldPassword = it
                        error = ""
                    },
                    label = { Text("Current Password") },
                    visualTransformation = if (showOldPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showOldPassword = !showOldPassword }) {
                            Icon(
                                imageVector = if (showOldPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showOldPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        error = ""
                    },
                    label = { Text("New Password") },
                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showNewPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        error = ""
                    },
                    label = { Text("Confirm New Password") },
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> {
                            error = "All fields are required"
                        }
                        newPassword != confirmPassword -> {
                            error = "Passwords do not match"
                        }
                        newPassword.length < 6 -> {
                            error = "Password must be at least 6 characters"
                        }
                        else -> {
                            onConfirm(oldPassword, newPassword)
                        }
                    }
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
