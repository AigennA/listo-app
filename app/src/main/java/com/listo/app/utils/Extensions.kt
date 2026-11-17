package com.listo.app.utils

import androidx.compose.ui.graphics.Color
import java.util.Locale

fun String.toComposeColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (e: Exception) {
    Color(0xFF2ECC71)
}

fun Int.toPercent(total: Int): Float =
    if (total == 0) 0f else (this.toFloat() / total.toFloat()).coerceIn(0f, 1f)

/** Formats a money amount with the ₺ symbol, dropping decimals when whole. */
fun formatPrice(value: Double): String =
    if (value % 1.0 == 0.0) "${value.toInt()} ₺"
    else String.format(Locale.getDefault(), "%.2f ₺", value)
