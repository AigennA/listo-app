package com.listo.app.utils

import android.content.res.Resources
import android.os.Build
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Picks the locale prices are formatted with. The currency follows the device
 * **region** (country), never the in-app language: an English UI on a phone set
 * to Sweden must still show kronor. [LocaleHelper] replaces the default locale
 * with a country-less one, so the region is read from the system configuration.
 */
object PriceLocale {

    fun current(): Locale = resolve(Locale.getDefault().language, systemCountry())

    /**
     * Returns a locale for [country] whose currency symbol is a real symbol
     * (e.g. "₺", "kr") rather than the ISO code ("TRY", "SEK"). A locale in
     * [language] is preferred; otherwise one of the country's own locales is used.
     */
    fun resolve(language: String, country: String): Locale {
        if (country.isBlank()) return Locale(language)
        val currency = currencyOf(Locale(language, country)) ?: return Locale(language, country)

        val candidates = Locale.getAvailableLocales()
            .filter { it.country == country && it.variant.isEmpty() && it.script.isEmpty() }
            .sortedByDescending { it.language == language }

        return candidates.firstOrNull { currency.getSymbol(it) != currency.currencyCode }
            ?: candidates.firstOrNull()
            ?: Locale(language, country)
    }

    private fun systemCountry(): String {
        val config = Resources.getSystem().configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales[0].country
        } else {
            @Suppress("DEPRECATION")
            config.locale.country
        }
    }

    internal fun currencyOf(locale: Locale): Currency? =
        runCatching { Currency.getInstance(locale) }.getOrNull()
}

/**
 * Formats a money amount in the currency of [locale]'s region, dropping the
 * decimals when the amount is whole. Without a region it formats a plain number.
 */
fun formatPrice(value: Double, locale: Locale = PriceLocale.current()): String {
    val decimals = if (value % 1.0 == 0.0) 0 else 2
    val format = if (PriceLocale.currencyOf(locale) != null) {
        NumberFormat.getCurrencyInstance(locale)
    } else {
        NumberFormat.getNumberInstance(locale).apply { isGroupingUsed = false }
    }
    format.minimumFractionDigits = decimals
    format.maximumFractionDigits = decimals
    return format.format(value)
}
