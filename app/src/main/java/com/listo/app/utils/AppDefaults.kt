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
}
