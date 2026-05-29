package com.listo.app.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary          = LisGreen,
    onPrimary        = LisWhite,
    primaryContainer = LisGreenLight,
    onPrimaryContainer = LisGreenDark,

    secondary          = LisPurple,
    onSecondary        = LisWhite,
    secondaryContainer = LisPurpleLight,
    onSecondaryContainer = LisPurpleDark,

    background    = LisWhite,
    onBackground  = LisTextPrimary,
    surface       = LisBg,
    onSurface     = LisTextPrimary,
    surfaceVariant = LisBg,
    onSurfaceVariant = LisTextSecondary,

    outline      = LisDivider,
    outlineVariant = LisTextHint,
    error        = LisError,
)

private val DarkColorScheme = darkColorScheme(
    primary          = LisGreen,
    onPrimary        = LisDarkBg,
    primaryContainer = LisGreenDark,
    onPrimaryContainer = LisGreenLight,

    secondary          = LisPurple,
    onSecondary        = LisDarkBg,
    secondaryContainer = LisPurpleDark,
    onSecondaryContainer = LisPurpleLight,

    background    = LisDarkBg,
    onBackground  = LisDarkTextPrimary,
    surface       = LisDarkSurface,
    onSurface     = LisDarkTextPrimary,
    surfaceVariant = LisDarkCard,
    onSurfaceVariant = LisDarkTextSecondary,

    outline      = LisDarkDivider,
    outlineVariant = LisDarkDivider,
    error        = LisError,
)

@Composable
fun LisToTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = LisToTypography,
        shapes      = LisToShapes,
        content     = content
    )
}
