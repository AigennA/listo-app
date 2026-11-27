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

    /**
     * Currency the user chose in Settings (ISO code like "SEK"), or null to
     * follow the device region. Set at start-up and when the setting changes.
     */
    @Volatile
    var chosenCurrency: String? = null

    fun current(): Locale = resolve(Locale.getDefault().language, systemCountry(), chosenCurrency)

    /** The locale prices would use with no currency chosen: the device region's. */
    fun regional(): Locale = resolve(Locale.getDefault().language, systemCountry())

    /**
     * Returns a locale whose currency symbol is a real symbol (e.g. "₺", "kr")
     * rather than the ISO code ("TRY", "SEK"). With [currencyCode] the locale
     * shows that currency whatever the region; otherwise it is [country]'s
     * currency. A locale in [language] is preferred.
     */
    fun resolve(language: String, country: String, currencyCode: String? = null): Locale {
        chosen(currencyCode)?.let { currency -> forCurrency(currency, language)?.let { return it } }
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

    private fun chosen(code: String?): Currency? =
        code?.let { runCatching { Currency.getInstance(it) }.getOrNull() }

    /** A locale that writes [currency] with its own symbol, in [language] if one exists. */
    private fun forCurrency(currency: Currency, language: String): Locale? =
        Locale.getAvailableLocales()
            .filter { it.country.isNotEmpty() && it.variant.isEmpty() && it.script.isEmpty() && currencyOf(it) == currency }
            .sortedByDescending { it.language == language }
            .firstOrNull { currency.getSymbol(it) != currency.currencyCode }
}

/** Currencies offered in Settings; null means "follow the phone's region". */
object CurrencyChoices {
    val all: List<String?> = listOf(
        null, "SEK", "TRY", "EUR", "USD", "GBP", "NOK", "DKK", "CHF", "PLN", "CAD", "AUD", "JPY"
    )
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
