package com.listo.app.utils

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.listo.app.R
import java.util.Locale

/** Translated display names for the built-in category keys of [GroceryCatalog]. */
object CategoryLabels {

    private val labels: Map<String, Int> = mapOf(
        GroceryCatalog.PRODUCE to R.string.category_builtin_produce,
        GroceryCatalog.DAIRY to R.string.category_builtin_dairy,
        GroceryCatalog.MEAT to R.string.category_builtin_meat,
        GroceryCatalog.BAKERY to R.string.category_builtin_bakery,
        GroceryCatalog.DRINKS to R.string.category_builtin_drinks,
        GroceryCatalog.SNACKS to R.string.category_builtin_snacks,
        GroceryCatalog.CLEANING to R.string.category_builtin_cleaning,
        GroceryCatalog.BREAKFAST to R.string.category_builtin_breakfast,
        GroceryCatalog.PANTRY to R.string.category_builtin_pantry,
        GroceryCatalog.UNCATEGORIZED to R.string.category_other
    )

    /** Label for a built-in key, or null for a category the user named. */
    @StringRes
    fun labelRes(name: String): Int? = labels[name]
}

/** Name to show for a category: translated if built in, as typed otherwise. */
@Composable
fun categoryLabel(name: String): String =
    CategoryLabels.labelRes(name)?.let { stringResource(it) } ?: name

/** The item catalog in the current app language, rebuilt when it changes. */
@Composable
fun rememberItemCatalog(): ItemCatalog {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(configuration) { loadItemCatalog(context) }
}

/** Languages Listo ships item names in, besides English. */
private val CATALOG_LANGUAGES = listOf("tr", "sv")

private fun loadItemCatalog(context: Context): ItemCatalog {
    fun arraysIn(locale: Locale): Map<String, List<String>> {
        val config = Configuration(context.resources.configuration).apply { setLocale(locale) }
        val resources = context.createConfigurationContext(config).resources
        return GroceryCatalog.itemArrays.mapValues { (_, res) -> resources.getStringArray(res).toList() }
    }
    val current = context.resources
    return ItemCatalog.of(
        english = arraysIn(Locale.ENGLISH),
        localized = GroceryCatalog.itemArrays.mapValues { (_, res) -> current.getStringArray(res).toList() },
        // Names typed in any supported language are understood, whatever the app's language.
        otherLanguages = CATALOG_LANGUAGES.map { arraysIn(Locale.forLanguageTag(it)) }
    )
}
