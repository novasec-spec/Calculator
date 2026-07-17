package com.novasec.auth.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NovaDarkColorScheme = darkColorScheme(
    primary = NovaPrimary,
    onPrimary = NovaOnPrimary,
    primaryContainer = NovaPrimaryDark,
    onPrimaryContainer = NovaOnPrimary,
    secondary = NovaSecondary,
    onSecondary = NovaOnPrimary,
    secondaryContainer = NovaSecondaryDark,
    onSecondaryContainer = NovaOnPrimary,
    tertiary = NovaAccentCyan,
    onTertiary = NovaOnPrimary,
    tertiaryContainer = NovaAccentCyan.copy(alpha = 0.2f),
    onTertiaryContainer = NovaAccentCyan,
    background = NovaBackground,
    onBackground = NovaOnBackground,
    surface = NovaSurface,
    onSurface = NovaOnSurface,
    surfaceVariant = NovaSurfaceVariant,
    onSurfaceVariant = NovaOnSurfaceVariant,
    surfaceTint = NovaPrimary,
    inverseSurface = NovaOnBackground,
    inverseOnSurface = NovaBackground,
    error = NovaError,
    onError = NovaOnError,
    errorContainer = NovaErrorContainer,
    onErrorContainer = NovaError,
    outline = NovaOnSurfaceVariant,
    outlineVariant = NovaSurfaceVariant,
    scrim = NovaBackground.copy(alpha = 0.8f)
)

private val NovaLightColorScheme = lightColorScheme(
    primary = NovaPrimary,
    onPrimary = NovaOnPrimary,
    primaryContainer = NovaPrimaryLight,
    onPrimaryContainer = NovaPrimaryDark,
    secondary = NovaSecondary,
    onSecondary = NovaOnPrimary,
    secondaryContainer = NovaSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = NovaSecondaryDark,
    tertiary = NovaAccentCyan,
    onTertiary = NovaOnPrimary,
    tertiaryContainer = NovaAccentCyan.copy(alpha = 0.1f),
    onTertiaryContainer = NovaAccentCyan,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    surfaceTint = NovaPrimary,
    inverseSurface = Color(0xFF1E293B),
    inverseOnSurface = Color(0xFFF1F5F9),
    error = NovaError,
    onError = NovaOnError,
    errorContainer = Color(0xFFFEF2F2),
    onErrorContainer = NovaError,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    scrim = Color(0xFF000000).copy(alpha = 0.5f)
)

@Composable
fun NovaSecTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> NovaDarkColorScheme
        else -> NovaLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NovaTypography,
        content = content
    )
}
