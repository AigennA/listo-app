package com.listo.app.data.models

import com.listo.app.ui.theme.ColorContrast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccentColorPaletteTest {

    @Test
    fun `every accent has a distinct primary color`() {
        val primaries = AccentColor.entries.map { it.primary }
        assertEquals(primaries.size, primaries.toSet().size)
    }

    @Test
    fun `every accent shade is fully opaque`() {
        AccentColor.entries.forEach { accent ->
            listOf(accent.primary, accent.dark, accent.light).forEach { argb ->
                assertEquals("$accent alpha", OPAQUE_ALPHA, argb ushr ALPHA_SHIFT)
            }
        }
    }

    @Test
    fun `dark shade is darker and light tint is lighter than primary`() {
        AccentColor.entries.forEach { accent ->
            val primary = luminance(accent.primary)
            assertTrue("$accent dark", luminance(accent.dark) < primary)
            assertTrue("$accent light", luminance(accent.light) > primary)
        }
    }

    private fun luminance(argb: Long): Double = ColorContrast.luminance(argb.toInt())

    private companion object {
        const val OPAQUE_ALPHA = 0xFFL
        const val ALPHA_SHIFT = 24
    }
}
