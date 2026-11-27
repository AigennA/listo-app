package com.listo.app.utils

import android.content.Context

/**
 * The currency the user picked in Settings, kept in synchronous preferences
 * so prices are right from the first frame (like [LocaleHelper]). Only the
 * symbol changes; amounts are never converted.
 */
object CurrencySetting {

    private const val PREFS = "currency_prefs"
    private const val KEY_CURRENCY = "currency_code"

    /** The chosen ISO code, or null for "follow the phone's region". */
    fun get(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_CURRENCY, null)

    /** Stores the choice and applies it to prices straight away. */
    fun set(context: Context, code: String?) {
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        if (code == null) editor.remove(KEY_CURRENCY) else editor.putString(KEY_CURRENCY, code)
        editor.apply()
        PriceLocale.chosenCurrency = code
    }

    /** Reads the stored choice into [PriceLocale]; call once at start-up. */
    fun apply(context: Context) {
        PriceLocale.chosenCurrency = get(context)
    }
}
