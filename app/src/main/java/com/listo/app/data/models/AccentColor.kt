package com.listo.app.data.models

/**
 * User-selectable accent palette. Stored by [name]; the hex values drive the
 * Material color scheme (see ui/theme/Theme.kt). Kept free of Compose types so
 * it can live in the data layer; display names are string resources
 * (see ui/theme/AccentColorLabels.kt).
 *
 * Declaration order is the picker order: by hue, neutrals last. Never rename
 * an entry — saved preferences refer to it by name; when removing one, map its
 * name in `retired` so those users land on a similar colour.
 */
enum class AccentColor(
    val primary: Long,
    val dark: Long,
    val light: Long
) {
    GREEN (0xFF2ECC71, 0xFF27AE60, 0xFFD5F5E3),
    TEAL  (0xFF00B894, 0xFF009E80, 0xFFCFF3EB),
    CYAN  (0xFF00ACC1, 0xFF00838F, 0xFFD1F2F7),
    BLUE  (0xFF3498DB, 0xFF2980B9, 0xFFD6EAF8),
    INDIGO(0xFF5C6BC0, 0xFF3F51B5, 0xFFE0E3F5),
    PURPLE(0xFF6C5CE7, 0xFF5641D8, 0xFFE8E5FC),
    ORCHID(0xFFAB47BC, 0xFF8E24AA, 0xFFF1DDF5),
    PINK  (0xFFE84393, 0xFFC72C7B, 0xFFFBDDEC),
    RED   (0xFFE74C3C, 0xFFC0392B, 0xFFFAD9D5),
    CORAL (0xFFE17055, 0xFFC85A42, 0xFFFAE0D8),
    MOCHA (0xFF8D6E63, 0xFF6D4C41, 0xFFEFE5E1),
    SLATE (0xFF607D8B, 0xFF455A64, 0xFFE0E8EC);

    companion object {
        /** Accents removed from the picker, mapped to the closest one still offered. */
        private val retired: Map<String, AccentColor> = mapOf(
            "ORANGE" to CORAL,
            "AMBER" to CORAL,
            "LIME" to GREEN
        )

        fun fromName(name: String?): AccentColor =
            entries.find { it.name == name } ?: retired[name] ?: GREEN
    }
}
