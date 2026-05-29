package com.listo.app.data.models

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.find { it.name == name } ?: SYSTEM
    }
}
