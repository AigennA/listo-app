package com.listo.app.ui.theme

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * WCAG 2.x contrast math on packed 0xAARRGGBB ints. Pure Kotlin, so the theme's
 * readability guarantees can be unit-tested without a device.
 */
object ColorContrast {

    /** WCAG AA minimum for normal-size text. */
    const val MIN_TEXT_RATIO = 4.5

    /** Relative luminance, 0.0 (black) to 1.0 (white). */
    fun luminance(argb: Int): Double =
        RED_WEIGHT * linear(argb shr RED_SHIFT) +
            GREEN_WEIGHT * linear(argb shr GREEN_SHIFT) +
            BLUE_WEIGHT * linear(argb)

    /** Contrast ratio between two colors, 1.0 (identical) to 21.0 (black on white). */
    fun ratio(first: Int, second: Int): Double {
        val a = luminance(first)
        val b = luminance(second)
        return (max(a, b) + FLARE) / (min(a, b) + FLARE)
    }

    /**
     * Returns [color] unchanged when it already reaches [minRatio] against [background];
     * otherwise the smallest blend toward black (light background) or white (dark
     * background) that does, so the hue stays recognisable.
     */
    fun ensure(color: Int, background: Int, minRatio: Double): Int {
        if (ratio(color, background) >= minRatio) return color
        val target = if (luminance(background) > MID_LUMINANCE) BLACK else WHITE
        var low = 0.0
        var high = 1.0
        repeat(SEARCH_STEPS) {
            val mid = (low + high) / 2
            if (ratio(blend(color, target, mid), background) >= minRatio) high = mid else low = mid
        }
        return blend(color, target, high)
    }

    private fun linear(channel: Int): Double {
        val c = (channel and CHANNEL_MASK) / CHANNEL_MAX
        return if (c <= LINEAR_LIMIT) c / LINEAR_DIVISOR else ((c + GAMMA_OFFSET) / (1 + GAMMA_OFFSET)).pow(GAMMA)
    }

    private fun blend(from: Int, to: Int, fraction: Double): Int {
        fun mix(shift: Int): Int {
            val a = (from shr shift) and CHANNEL_MASK
            val b = (to shr shift) and CHANNEL_MASK
            return (a + (b - a) * fraction).roundToInt() shl shift
        }
        return OPAQUE_ALPHA or mix(RED_SHIFT) or mix(GREEN_SHIFT) or mix(BLUE_SHIFT)
    }

    // WCAG 2.x relative-luminance constants for sRGB.
    private const val RED_WEIGHT = 0.2126
    private const val GREEN_WEIGHT = 0.7152
    private const val BLUE_WEIGHT = 0.0722
    private const val LINEAR_LIMIT = 0.03928
    private const val LINEAR_DIVISOR = 12.92
    private const val GAMMA_OFFSET = 0.055
    private const val GAMMA = 2.4
    private const val FLARE = 0.05

    /** Luminance with equal contrast against black and white; above it a background counts as light. */
    private const val MID_LUMINANCE = 0.179

    /** Halvings in [ensure]; 20 pins the blend far below one 8-bit channel step. */
    private const val SEARCH_STEPS = 20

    private const val RED_SHIFT = 16
    private const val GREEN_SHIFT = 8
    private const val BLUE_SHIFT = 0
    private const val CHANNEL_MASK = 0xFF
    private const val CHANNEL_MAX = 255.0
    private const val OPAQUE_ALPHA = 0xFF000000.toInt()
    private const val BLACK = OPAQUE_ALPHA
    private const val WHITE = 0xFFFFFFFF.toInt()
}
