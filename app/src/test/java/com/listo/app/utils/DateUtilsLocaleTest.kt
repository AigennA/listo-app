package com.listo.app.utils

import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Locale

/**
 * The app can switch language at runtime (LocaleHelper sets the default
 * locale), so dates must follow the locale in force when they are formatted,
 * not the one at first use.
 */
class DateUtilsLocaleTest {

    private lateinit var original: Locale
    private val nov19 = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 19, 9, 0) }.timeInMillis

    @Before fun remember() { original = Locale.getDefault() }
    @After fun restore() { Locale.setDefault(original) }

    @Test
    fun `short date follows a language change made after first use`() {
        Locale.setDefault(Locale.ENGLISH)
        val english = DateUtils.formatShortDate(nov19)

        Locale.setDefault(Locale("sv", "SE"))
        val swedish = DateUtils.formatShortDate(nov19)

        assertTrue(english, english.startsWith("Nov"))
        assertTrue(swedish, swedish.startsWith("nov"))
    }

    @Test
    fun `full date follows a language change made after first use`() {
        Locale.setDefault(Locale.ENGLISH)
        DateUtils.formatDate(nov19)

        Locale.setDefault(Locale("tr", "TR"))
        val turkish = DateUtils.formatDate(nov19)

        assertTrue(turkish, turkish.contains("Kas"))
    }
}
