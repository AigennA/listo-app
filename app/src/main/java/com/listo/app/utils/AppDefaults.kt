package com.listo.app.utils

/** Centralised tunable values, kept out of UI/business code (no magic numbers). */
object AppDefaults {
    // Budget editor
    const val BUDGET_MAX = 2000.0
    const val BUDGET_STEP = 10.0

    // Item price stepper
    const val PRICE_STEP = 1.0

    // Daily reminder default time
    const val DEFAULT_REMINDER_HOUR = 18
    const val DEFAULT_REMINDER_MINUTE = 0

    // How late a list reminder may fire. Inexact alarms avoid SCHEDULE_EXACT_ALARM,
    // which Google Play restricts to alarm-clock style apps.
    const val REMINDER_WINDOW_MS = 10L * 60L * 1000L
}
