package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val CyberDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = CyberBlack,
    primaryContainer = CyberSurfaceVariant,
    onPrimaryContainer = NeonGreen,
    secondary = CyberCyan,
    onSecondary = CyberBlack,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = CyberCyan,
    tertiary = CyberAmber,
    onTertiary = CyberBlack,
    background = CyberBlack,
    onBackground = TextPrimary,
    surface = CyberDark,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurface,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder,
    outlineVariant = CyberBorderBright,
    error = CyberCrimson,
    onError = CyberBlack
)

private val CyberLightColorScheme = lightColorScheme(
    primary = NeonGreen,
    onPrimary = CyberBlack,
    primaryContainer = CyberSurfaceVariant,
    onPrimaryContainer = NeonGreen,
    secondary = CyberCyan,
    onSecondary = CyberBlack,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = CyberCyan,
    tertiary = CyberAmber,
    onTertiary = CyberBlack,
    background = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
    onBackground = CyberBlack,
    surface = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
    onSurface = CyberBlack,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onSurfaceVariant = TextPrimary,
    outline = CyberBorder,
    outlineVariant = CyberBorderBright,
    error = CyberCrimson,
    onError = CyberBlack
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true, // Enable dynamic color by default for the 'cyber' lab aesthetic as requested
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CyberDarkColorScheme
        else -> CyberLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

