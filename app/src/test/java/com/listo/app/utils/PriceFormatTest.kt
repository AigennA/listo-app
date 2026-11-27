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
    fun `a chosen currency shows its own symbol whatever the region`() {
        val inSweden = formatPrice(25.0, PriceLocale.resolve(language = "en", country = "US", currencyCode = "SEK"))
        assertTrue(inSweden, inSweden.contains("kr"))
        val lira = formatPrice(25.0, PriceLocale.resolve(language = "sv", country = "SE", currencyCode = "TRY"))
        assertTrue(lira, lira.contains("₺"))
        val euro = formatPrice(25.0, PriceLocale.resolve(language = "tr", country = "TR", currencyCode = "EUR"))
        assertTrue(euro, euro.contains("€"))
        val dollar = formatPrice(25.0, PriceLocale.resolve(language = "sv", country = "SE", currencyCode = "USD"))
        assertTrue(dollar, dollar.contains("$"))
    }

    @Test
    fun `no chosen currency keeps following the region`() {
        assertEquals("SE", PriceLocale.resolve(language = "en", country = "SE", currencyCode = null).country)
    }

    @Test
    fun `an unknown currency code is ignored`() {
        val price = formatPrice(17.0, PriceLocale.resolve(language = "en", country = "SE", currencyCode = "XYZ1"))
        assertTrue(price, price.contains("kr"))
    }

    @Test
    fun `the currency list starts with automatic and holds the common ones`() {
        assertEquals(null, CurrencyChoices.all.first())
        assertTrue(CurrencyChoices.all.containsAll(listOf("SEK", "TRY", "EUR", "USD", "GBP", "NOK", "DKK")))
        assertEquals(CurrencyChoices.all.size, CurrencyChoices.all.toSet().size)
    }

    @Test
    fun `missing region falls back to a plain number`() {
        assertEquals("10.50", formatPrice(10.5, PriceLocale.resolve(language = "en", country = "")))
    }
}
