package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SoftBluePrimary,
    secondary = PlayfulOrange,
    tertiary = MintGreen,
    background = CosmicBg,
    surface = MediumGray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE2E2E2),
    onSurface = Color(0xFFE2E2E2)
)

private val LightColorScheme = lightColorScheme(
    primary = SoftBluePrimary,
    secondary = PlayfulOrange,
    tertiary = MintGreen,
    background = BoldIceBg,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = BoldDarkText,
    onSurface = BoldDarkText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Forced false for children educational theme color high contrast consistency
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Force LightColorScheme to guarantee consistent high-contrast, beautiful playful branding and dark-text-on-light-bg visibility
    val colorScheme = LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
