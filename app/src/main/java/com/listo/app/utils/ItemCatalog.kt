package com.listo.app.utils

import java.util.Locale

/**
 * A common grocery item. [key] is its English name, used to pick an icon;
 * [name] is what the user sees in the current language; [category] is a
 * [GroceryCatalog] category key.
 */
data class CatalogItem(val key: String, val name: String, val category: String)

/**
 * Type-ahead suggestions and automatic categories for common grocery items,
 * in the current language. Build it with [of]; on a device use
 * [rememberItemCatalog].
 */
class ItemCatalog private constructor(private val items: List<CatalogItem>) {

    fun suggest(query: String, limit: Int = 6): List<CatalogItem> {
        val q = query.normalized()
        if (q.isEmpty()) return emptyList()
        val starts = items.filter { it.name.normalized().startsWith(q) }
        val contains = items.filter {
            val n = it.name.normalized()
            !n.startsWith(q) && n.contains(q)
        }
        return (starts + contains).distinctBy { it.name }.take(limit)
    }

    fun itemsForCategory(category: String): List<CatalogItem> =
        items.filter { it.category.equals(category, ignoreCase = true) }

    /** Category of a typed name, matched in the current language or in English. */
    fun categoryFor(name: String): String? {
        val q = name.normalized()
        if (q.isEmpty()) return null
        return items.firstOrNull { q == it.name.normalized() || q == it.key.normalized() }?.category
            ?: items.firstOrNull { q.hasWordStartingWith(it.name.normalized()) || q.hasWordStartingWith(it.key.normalized()) }
                ?.category
    }

    /** English key of a catalog item typed in any supported language, or null. */
    fun keyFor(name: String): String? {
        val q = name.normalized()
        return items.firstOrNull { q == it.name.normalized() || q == it.key.normalized() }?.key
    }

    companion object {
        /**
         * Pairs each [localized] name with the [english] key at the same position.
         * A category whose translation has a different length is shown in English
         * rather than paired wrongly.
         */
        fun of(english: Map<String, List<String>>, localized: Map<String, List<String>>): ItemCatalog {
            val items = english.flatMap { (category, keys) ->
                val names = localized[category]?.takeIf { it.size == keys.size } ?: keys
                keys.zip(names) { key, name -> CatalogItem(key, name, category) }
            }
            return ItemCatalog(items)
        }

        private fun String.normalized(): String = trim().lowercase(Locale.ROOT)

        private fun String.hasWordStartingWith(word: String): Boolean {
            var index = indexOf(word)
            while (index >= 0) {
                if (index == 0 || this[index - 1].isWhitespace()) return true
                index = indexOf(word, index + 1)
            }
            return false
        }
    }
}
