package com.listo.app.data.models

/** Display names are string resources (see ui/theme/ThemeLabels.kt). */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.find { it.name == name } ?: SYSTEM
    }
}
