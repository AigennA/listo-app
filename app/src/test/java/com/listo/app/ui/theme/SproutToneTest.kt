package com.listo.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.listo.app.data.models.AccentColor
import org.junit.Assert.assertEquals
import org.junit.Test

class SproutToneTest {

    private fun toneFor(accent: AccentColor) = sproutToneFor(Color(accent.primary.toInt()))

    @Test
    fun `the sprout is green on non-green accents`() {
        listOf(AccentColor.PURPLE, AccentColor.ORCHID, AccentColor.RED, AccentColor.BLUE, AccentColor.SLATE)
            .forEach { assertEquals(it.name, SproutTones.GREEN, toneFor(it)) }
    }

    @Test
    fun `the sprout turns warm when the accent is itself green`() {
        listOf(AccentColor.GREEN, AccentColor.TEAL)
            .forEach { assertEquals(it.name, SproutTones.WARM, toneFor(it)) }
    }

    @Test
    fun `a grey accent keeps the green sprout`() {
        assertEquals(SproutTones.GREEN, sproutToneFor(Color(0xFF808080)))
    }
}
