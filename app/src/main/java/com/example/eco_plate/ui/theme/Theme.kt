package com.example.eco_plate.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4FD080),
    secondary = Color(0xFFFF6B35),
    tertiary = Color(0xFFFFC107),
    background = Color(0xFF111827),
    surface = Color(0xFF1F2937),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE5E7EB),
    onSurface = Color(0xFFE5E7EB),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2BAE66),
    primaryContainer = Color(0xFF4FD080),
    secondary = Color(0xFFFF6B35),
    secondaryContainer = Color(0xFFFFC107),
    tertiary = Color(0xFFFFC107),
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF3F4F6),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1F2937),
    onSurface = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF6B7280),
)

@Composable
fun EcoPlateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            // Find the Activity from the context
            val window = when (context) {
                is Activity -> context.window
                is android.content.ContextWrapper -> {
                    var ctx = context
                    while (ctx is android.content.ContextWrapper) {
                        if (ctx is Activity) {
                            break
                        }
                        ctx = ctx.baseContext as android.content.ContextWrapper
                    }
                    (ctx as? Activity)?.window
                }
                else -> null
            }
            
            window?.let {
                it.statusBarColor = Color.Transparent.toArgb()
                WindowCompat.setDecorFitsSystemWindows(it, false)
                val windowInsetsController = WindowCompat.getInsetsController(it, view)
                windowInsetsController?.isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
