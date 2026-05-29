package com.listo.app.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import java.util.Locale

/**
 * Applies an in-app language independent of the system locale. The choice is
 * stored in synchronous SharedPreferences so it can be read in
 * [android.app.Activity.attachBaseContext] before resources are resolved.
 */
object LocaleHelper {

    private const val PREFS = "locale_prefs"
    private const val KEY_LANG = "app_language"

    /** "system" follows the device locale; otherwise an ISO code like "en" / "tr". */
    const val SYSTEM = "system"

    fun getLanguage(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, SYSTEM) ?: SYSTEM

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, language)
            .apply()
    }

    fun wrap(context: Context): Context {
        val language = getLanguage(context)
        if (language == SYSTEM) return context

        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLayoutDirection(locale)
        }
        return ContextWrapper(context.createConfigurationContext(config))
    }
}
