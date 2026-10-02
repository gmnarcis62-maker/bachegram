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
    primary = PrimaryOrangeNight,
    onPrimary = Color.Black,
    primaryContainer = PrimaryOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = SecondaryTurquoiseNight,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryTurquoiseDark,
    onSecondaryContainer = Color.White,
    tertiary = TertiaryGreen,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCAD2C5)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = PrimaryOrangeLight.copy(alpha = 0.3f),
    onPrimaryContainer = PrimaryOrangeDark,
    secondary = SecondaryTurquoise,
    onSecondary = Color.White,
    secondaryContainer = SecondaryTurquoiseLight.copy(alpha = 0.3f),
    onSecondaryContainer = SecondaryTurquoiseDark,
    tertiary = TertiaryGreen,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF6B5E51)
)

@Composable
fun BachegramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent cheerful branding for kids
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep backwards-compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    BachegramTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
