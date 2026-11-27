package com.listo.app.utils

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CurrencySettingTest {

    private val editor = mockk<SharedPreferences.Editor>(relaxed = true) {
        every { putString(any(), any()) } returns this
        every { remove(any()) } returns this
    }
    private val prefs = mockk<SharedPreferences> {
        every { edit() } returns editor
    }
    private val context = mockk<Context> {
        every { getSharedPreferences(any(), any()) } returns prefs
    }

    @After
    fun reset() { PriceLocale.chosenCurrency = null }

    @Test
    fun `nothing chosen means automatic`() {
        every { prefs.getString(any(), any()) } returns null
        assertNull(CurrencySetting.get(context))
    }

    @Test
    fun `a chosen currency is stored and used for prices at once`() {
        CurrencySetting.set(context, "SEK")
        verify { editor.putString(any(), "SEK") }
        assertEquals("SEK", PriceLocale.chosenCurrency)
    }

    @Test
    fun `choosing automatic forgets the stored currency`() {
        CurrencySetting.set(context, null)
        verify { editor.remove(any()) }
        assertNull(PriceLocale.chosenCurrency)
    }
}
