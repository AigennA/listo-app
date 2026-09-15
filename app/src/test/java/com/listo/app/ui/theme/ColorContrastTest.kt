package com.listo.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorContrastTest {

    @Test
    fun `black on white is the maximum ratio of 21`() {
        assertEquals(MAX_RATIO, ColorContrast.ratio(WHITE, BLACK), TOLERANCE)
    }

    @Test
    fun `ratio does not depend on argument order`() {
        assertEquals(ColorContrast.ratio(AMBER, WHITE), ColorContrast.ratio(WHITE, AMBER), TOLERANCE)
    }

    @Test
    fun `ensure keeps a color that already has enough contrast`() {
        assertEquals(NAVY, ColorContrast.ensure(NAVY, WHITE, ColorContrast.MIN_TEXT_RATIO))
    }

    @Test
    fun `ensure darkens a light color on a light background just enough`() {
        val result = ColorContrast.ensure(AMBER, WHITE, ColorContrast.MIN_TEXT_RATIO)
        assertJustEnough(ColorContrast.ratio(result, WHITE))
        assertTrue(ColorContrast.luminance(result) < ColorContrast.luminance(AMBER))
        assertEquals(OPAQUE_ALPHA, result ushr ALPHA_SHIFT)
    }

    @Test
    fun `ensure lightens a dark color on a dark background just enough`() {
        val result = ColorContrast.ensure(NAVY, BLACK, ColorContrast.MIN_TEXT_RATIO)
        assertJustEnough(ColorContrast.ratio(result, BLACK))
        assertTrue(ColorContrast.luminance(result) > ColorContrast.luminance(NAVY))
    }

    private fun assertJustEnough(ratio: Double) {
        assertTrue("reaches AA, was $ratio", ratio >= ColorContrast.MIN_TEXT_RATIO)
        assertTrue("minimal change, was $ratio", ratio < ColorContrast.MIN_TEXT_RATIO + MAX_OVERSHOOT)
    }

    private companion object {
        const val WHITE = 0xFFFFFFFF.toInt()
        const val BLACK = 0xFF000000.toInt()
        const val AMBER = 0xFFF39C12.toInt()
        const val NAVY = 0xFF1A237E.toInt()
        const val MAX_RATIO = 21.0
        const val TOLERANCE = 0.01
        const val MAX_OVERSHOOT = 0.25
        const val OPAQUE_ALPHA = 0xFF
        const val ALPHA_SHIFT = 24
    }
}
