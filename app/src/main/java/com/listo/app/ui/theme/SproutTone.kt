package com.listo.app.ui.theme

import androidx.compose.ui.graphics.Color

/** The two colours the sprout on the Lists tab can take. */
object SproutTones {
    /** The brand sprout green. */
    val GREEN = Color(0xFF40916C)

    /** A warm tone for when the accent is green itself, so the sprout still stands out. */
    val WARM = Color(0xFFF4A259)
}

/** Hue range (degrees) treated as "green": green and teal accents, not cyan. */
private val GREEN_HUES = 70f..175f

/** Below this saturation a colour is a grey and has no meaningful hue. */
private const val MIN_SATURATION = 0.2f

/**
 * Colour for the sprout growing out of the Lists tab icon, which otherwise
 * takes the accent [primary]: green, unless the accent is green too.
 */
fun sproutToneFor(primary: Color): Color {
    val max = maxOf(primary.red, primary.green, primary.blue)
    val min = minOf(primary.red, primary.green, primary.blue)
    val delta = max - min
    if (max == 0f || delta / max < MIN_SATURATION) return SproutTones.GREEN

    val hue = when (max) {
        primary.red -> 60f * (((primary.green - primary.blue) / delta) % 6f)
        primary.green -> 60f * (((primary.blue - primary.red) / delta) + 2f)
        else -> 60f * (((primary.red - primary.green) / delta) + 4f)
    }.let { if (it < 0f) it + 360f else it }

    return if (hue in GREEN_HUES) SproutTones.WARM else SproutTones.GREEN
}
