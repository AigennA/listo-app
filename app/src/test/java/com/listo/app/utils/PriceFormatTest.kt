package com.listo.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class PriceFormatTest {

    private val usEnglish = Locale("en", "US")
    private val turkey = Locale("tr", "TR")

    @Test
    fun `whole amounts drop decimals`() {
        assertEquals("$10", formatPrice(10.0, usEnglish))
    }

    @Test
    fun `fractional amounts keep two decimals`() {
        assertEquals("$10.50", formatPrice(10.5, usEnglish))
    }

    @Test
    fun `turkish region shows lira`() {
        assertTrue(formatPrice(17.0, turkey).contains("₺"))
    }

    @Test
    fun `swedish region shows kronor, not lira`() {
        val price = formatPrice(17.0, PriceLocale.resolve(language = "en", country = "SE"))
        assertTrue(price, price.contains("kr"))
        assertFalse(price, price.contains("₺"))
    }

    @Test
    fun `english app language in turkey still shows lira`() {
        val price = formatPrice(17.0, PriceLocale.resolve(language = "en", country = "TR"))
        assertTrue(price, price.contains("₺"))
    }

    @Test
    fun `region follows the country, not the app language`() {
        assertEquals("SE", PriceLocale.resolve(language = "tr", country = "SE").country)
    }

    @Test
    fun `missing region falls back to a plain number`() {
        assertEquals("10.50", formatPrice(10.5, PriceLocale.resolve(language = "en", country = "")))
    }
}
