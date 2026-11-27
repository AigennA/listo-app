package com.listo.app.utils

import com.listo.app.data.models.QuantityUnit
import java.text.NumberFormat
import java.util.Locale

/** −/+ steps for an amount, kept at or above the unit's smallest step. */
object QuantityStepper {

    /** An item with no amount yet counts as one of [unit]'s starting amount. */
    fun up(amount: Double?, unit: QuantityUnit): Double = (amount ?: unit.defaultAmount) + unit.step

    fun down(amount: Double?, unit: QuantityUnit): Double =
        ((amount ?: unit.defaultAmount) - unit.step).coerceAtLeast(unit.min)
}

/** Typing a price by hand. */
object PriceInput {

    /** The typed price, or null when the text is not a price. Comma and dot both work. */
    fun parse(text: String): Double? =
        text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0.0 }

    /** Keeps digits and the first decimal sign, so the field never holds junk. */
    fun clean(text: String): String {
        var seenSign = false
        return buildString {
            text.forEach { c ->
                when {
                    c.isDigit() -> append(c)
                    (c == ',' || c == '.') && !seenSign -> { append(c); seenSign = true }
                }
            }
        }
    }
}

/** How fast a held −/+ button repeats: a pause, then quicker and quicker. */
object RepeatSchedule {
    private const val FIRST_DELAY_MS = 400L
    private const val EARLY_DELAY_MS = 120L
    private const val FAST_DELAY_MS = 50L
    private const val EARLY_REPEATS = 10

    /** Wait before repeat number [index] (0 is the first repeat after pressing). */
    fun delayMillis(index: Int): Long = when {
        index == 0 -> FIRST_DELAY_MS
        index < EARLY_REPEATS -> EARLY_DELAY_MS
        else -> FAST_DELAY_MS
    }
}

/** An amount for display: "2", "1.5" (or "1,5" where a comma is the decimal sign). */
fun formatAmount(amount: Double, locale: Locale = Locale.getDefault()): String {
    val format = NumberFormat.getNumberInstance(locale).apply {
        isGroupingUsed = false
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    return format.format(amount)
}
