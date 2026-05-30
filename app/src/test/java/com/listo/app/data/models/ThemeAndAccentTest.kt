package com.listo.app.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeAndAccentTest {

    @Test
    fun `themeMode fromName parses known and falls back to system`() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromName("DARK"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromName("LIGHT"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName("nope"))
    }

    @Test
    fun `accentColor fromName parses known and falls back to green`() {
        assertEquals(AccentColor.PURPLE, AccentColor.fromName("PURPLE"))
        assertEquals(AccentColor.GREEN, AccentColor.fromName(null))
        assertEquals(AccentColor.GREEN, AccentColor.fromName("nope"))
    }
}
