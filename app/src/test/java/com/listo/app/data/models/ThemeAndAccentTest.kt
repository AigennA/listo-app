package com.listo.app.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun `persisted accent names never change`() {
        assertTrue(AccentColor.entries.map { it.name }.containsAll(PERSISTED_ACCENT_NAMES))
    }

    @Test
    fun `retired accents move to the closest remaining colour`() {
        assertEquals(AccentColor.CORAL, AccentColor.fromName("ORANGE"))
        assertEquals(AccentColor.CORAL, AccentColor.fromName("AMBER"))
        assertEquals(AccentColor.GREEN, AccentColor.fromName("LIME"))
    }

    @Test
    fun `the picker shows two full rows of six`() {
        assertEquals(12, AccentColor.entries.size)
    }

    private companion object {
        /** Names already written to users' preferences; renaming one resets their choice. */
        val PERSISTED_ACCENT_NAMES = listOf(
            "GREEN", "TEAL", "BLUE", "INDIGO", "PURPLE", "PINK", "RED", "CORAL",
            "CYAN", "ORCHID", "MOCHA", "SLATE"
        )
    }
}
