package com.listo.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.listo.app.data.models.AccentColor

private fun lightScheme(primary: Color, primaryDark: Color, primaryLight: Color) = lightColorScheme(
    primary          = primary,
    onPrimary        = LisWhite,
    primaryContainer = primaryLight,
    onPrimaryContainer = primaryDark,

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

private fun darkScheme(primary: Color, primaryDark: Color, primaryLight: Color) = darkColorScheme(
    primary          = primary,
    onPrimary        = LisDarkBg,
    primaryContainer = primaryDark,
    onPrimaryContainer = primaryLight,

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
    accent: AccentColor = AccentColor.GREEN,
    content: @Composable () -> Unit
) {
    // onPrimary is the background color in both schemes, so toning primary against the
    // background also keeps text on primary readable.
    val primary      = accent.primaryOn(if (darkTheme) LisDarkBg else LisWhite)
    val primaryDark  = Color(accent.dark)
    val primaryLight = Color(accent.light)
    val colorScheme = if (darkTheme) {
        darkScheme(primary, primaryDark, primaryLight)
    } else {
        lightScheme(primary, primaryDark, primaryLight)
    }

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
