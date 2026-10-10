package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = JaPurpleLight,
    onPrimary = JaDeepBackground,
    primaryContainer = JaPurpleDark,
    onPrimaryContainer = JaTextPrimary,
    secondary = JaCyanAccent,
    onSecondary = JaDeepBackground,
    secondaryContainer = JaSurfaceCard,
    onSecondaryContainer = JaCyanAccent,
    tertiary = JaPinkAccent,
    onTertiary = JaDeepBackground,
    background = JaDeepBackground,
    onBackground = JaTextPrimary,
    surface = JaSurfaceDark,
    onSurface = JaTextPrimary,
    surfaceVariant = JaSurfaceCard,
    onSurfaceVariant = JaTextSecondary,
    outline = JaSurfaceBorder
)

private val LightColorScheme = darkColorScheme(
    primary = JaPurplePrimary,
    onPrimary = JaTextPrimary,
    primaryContainer = JaPurpleLight,
    onPrimaryContainer = JaDeepBackground,
    secondary = JaCyanAccent,
    onSecondary = JaDeepBackground,
    tertiary = JaPinkAccent,
    onTertiary = JaDeepBackground,
    background = JaDeepBackground,
    onBackground = JaTextPrimary,
    surface = JaSurfaceDark,
    onSurface = JaTextPrimary,
    surfaceVariant = JaSurfaceCard,
    onSurfaceVariant = JaTextSecondary,
    outline = JaSurfaceBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep signature anime styling consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Use Anime aesthetic
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
