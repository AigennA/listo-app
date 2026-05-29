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
    TEAL  ("Teal",   0xFF00B894, 0xFF009E80, 0xFFCFF3EB),
    BLUE  ("Blue",   0xFF3498DB, 0xFF2980B9, 0xFFD6EAF8),
    INDIGO("Indigo", 0xFF5C6BC0, 0xFF3F51B5, 0xFFE0E3F5),
    PURPLE("Purple", 0xFF6C5CE7, 0xFF5641D8, 0xFFE8E5FC),
    PINK  ("Pink",   0xFFE84393, 0xFFC72C7B, 0xFFFBDDEC),
    RED   ("Red",    0xFFE74C3C, 0xFFC0392B, 0xFFFAD9D5),
    CORAL ("Coral",  0xFFE17055, 0xFFC85A42, 0xFFFAE0D8),
    ORANGE("Orange", 0xFFE67E22, 0xFFD2691E, 0xFFFCE5D0),
    AMBER ("Amber",  0xFFF39C12, 0xFFD4860B, 0xFFFDECCB);

    companion object {
        fun fromName(name: String?): AccentColor =
            entries.find { it.name == name } ?: GREEN
    }
}
