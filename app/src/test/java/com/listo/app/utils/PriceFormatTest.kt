package com.listo.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceFormatTest {

    @Test
    fun `whole amounts drop decimals`() {
        assertEquals("10 ₺", formatPrice(10.0))
    }

    @Test
    fun `fractional amounts keep the currency symbol`() {
        assertTrue(formatPrice(10.5).contains("₺"))
    }
}
