package com.example.eco_plate.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eco_plate.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserTypeSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            ComposeView(this).apply {
                setContent {
                    MaterialTheme(
                        colorScheme = lightColorScheme(
                            primary = Color(0xFF4CAF50),
                            secondary = Color(0xFF81C784),
                            tertiary = Color(0xFFFFA726)
                        )
                    ) {
                        UserTypeSelectionScreen(
                            onCustomerSelected = {
                                navigateToCustomerSignup()
                            },
                            onStoreSelected = {
                                navigateToStoreSignup()
                            },
                            onBackPressed = {
                                navigateToLogin()
                            }
                        )
                    }
                }
            }
        )
    }

    private fun navigateToCustomerSignup() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToStoreSignup() {
        val intent = Intent(this, StoreSignUpActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun UserTypeSelectionScreen(
    onCustomerSelected: () -> Unit,
    onStoreSelected: () -> Unit,
    onBackPressed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9),
                        Color(0xFFC8E6C9)
                    )
                )
            )
    ) {
        // Top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackPressed
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Welcome to EcoPlate",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Choose how you want to use EcoPlate",
                fontSize = 16.sp,
                color = Color(0xFF558B2F),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Customer Option
            UserTypeCard(
                icon = Icons.Default.Person,
                title = "I'm a Customer",
                description = "Find great deals on surplus food and help reduce food waste",
                backgroundColor = Color(0xFF4CAF50),
                onClick = onCustomerSelected
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Store Option
            UserTypeCard(
                icon = Icons.Default.Store,
                title = "I'm a Store Owner",
                description = "Sell surplus food, reduce waste, and connect with eco-conscious customers",
                backgroundColor = Color(0xFF388E3C),
                onClick = onStoreSelected
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info text
            Text(
                text = "You can always switch or add another account type later",
                fontSize = 13.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Login link at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            TextButton(
                onClick = onBackPressed // This will navigate to login
            ) {
                Text(
                    text = "Already have an account? Sign In",
                    color = Color(0xFF2E7D32),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTypeCard(
    icon: ImageVector,
    title: String,
    description: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
