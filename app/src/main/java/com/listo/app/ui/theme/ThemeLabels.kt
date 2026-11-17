package com.listo.app.ui.theme

import androidx.annotation.StringRes
import com.listo.app.R
import com.listo.app.data.models.AccentColor
import com.listo.app.data.models.ThemeMode

// Localized display names for the theme enums. Each `when` is exhaustive, so a new
// entry won't compile without a label.

@StringRes
fun AccentColor.labelRes(): Int = when (this) {
    AccentColor.GREEN  -> R.string.accent_green
    AccentColor.TEAL   -> R.string.accent_teal
    AccentColor.CYAN   -> R.string.accent_cyan
    AccentColor.BLUE   -> R.string.accent_blue
    AccentColor.INDIGO -> R.string.accent_indigo
    AccentColor.PURPLE -> R.string.accent_purple
    AccentColor.ORCHID -> R.string.accent_orchid
    AccentColor.PINK   -> R.string.accent_pink
    AccentColor.RED    -> R.string.accent_red
    AccentColor.CORAL  -> R.string.accent_coral
    AccentColor.ORANGE -> R.string.accent_orange
    AccentColor.AMBER  -> R.string.accent_amber
    AccentColor.LIME   -> R.string.accent_lime
    AccentColor.MOCHA  -> R.string.accent_mocha
    AccentColor.SLATE  -> R.string.accent_slate
}

@StringRes
fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT  -> R.string.theme_light
    ThemeMode.DARK   -> R.string.theme_dark
}
