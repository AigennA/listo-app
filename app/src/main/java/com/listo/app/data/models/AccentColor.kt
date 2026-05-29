package com.listo.app.data.models

/**
 * User-selectable accent palette. Stored by [name]; the hex values drive the
 * Material color scheme (see ui/theme/Theme.kt). Kept free of Compose types so
 * it can live in the data layer.
 */
enum class AccentColor(
    val label: String,
    val primary: Long,
    val dark: Long,
    val light: Long
) {
    GREEN ("Green",  0xFF2ECC71, 0xFF27AE60, 0xFFD5F5E3),
    PURPLE("Purple", 0xFF6C5CE7, 0xFF5641D8, 0xFFE8E5FC),
    BLUE  ("Blue",   0xFF3498DB, 0xFF2980B9, 0xFFD6EAF8),
    CORAL ("Coral",  0xFFE17055, 0xFFC85A42, 0xFFFAE0D8),
    AMBER ("Amber",  0xFFF39C12, 0xFFD4860B, 0xFFFDECCB);

    companion object {
        fun fromName(name: String?): AccentColor =
            entries.find { it.name == name } ?: GREEN
    }
}
