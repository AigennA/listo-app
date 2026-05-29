package com.listo.app.data.models

enum class Priority(val label: String, val level: Int) {
    LOW("Low", 0),
    MEDIUM("Medium", 1),
    HIGH("High", 2);

    companion object {
        fun fromName(name: String): Priority =
            entries.find { it.name == name } ?: LOW
    }
}
