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
 *
 * Matching forgives what people actually type on a phone: missing accents
 * (a name typed without its accents still matches) and small typos
 * ("pinaple" finds "Pineapple").
 */
class ItemCatalog private constructor(items: List<CatalogItem>) {

    /** An item with its name and key prepared for matching. */
    private class Entry(val item: CatalogItem) {
        val name = item.name.folded()
        val key = item.key.folded()

        /** Name and key without spaces, for a two-word name typed as one word. */
        val compactName = name.compact()
        val compactKey = key.compact()
    }

    private val entries = items.map(::Entry)

    fun suggest(query: String, limit: Int = 6): List<CatalogItem> {
        val q = query.folded()
        if (q.length < MIN_SUGGEST_LENGTH) return emptyList()
        val compactQuery = q.compact()
        val exact = entries.filter { it.name == q }
        val starts = entries.filter { it.name.startsWith(q) || it.compactName.startsWith(compactQuery) }
        val contains = entries.filter { it.name.contains(q) }
        val typos = typoTolerance(q)
        val similar = if (typos == 0) emptyList() else entries
            .map { it to prefixDistance(q, it.name, typos) }
            .filter { (_, distance) -> distance <= typos }
            .sortedBy { (_, distance) -> distance }
            .map { (entry, _) -> entry }
        return (exact + starts + contains + similar)
            .map { it.item }
            .distinctBy { it.name }
            .take(limit)
    }

    fun itemsForCategory(category: String): List<CatalogItem> =
        entries.map { it.item }.filter { it.category.equals(category, ignoreCase = true) }

    /** Category of a typed name, matched as described in [match]. */
    fun categoryFor(name: String): String? = match(name)?.category

    /** English key of a typed name (for its icon), matched as described in [match]. */
    fun keyFor(name: String): String? = match(name)?.key

    /**
     * The catalog item a typed name refers to, in the current language or in
     * English: an exact name first (spaces optional), then the longest known
     * name inside it (a longer name such as "honey melon" beats "honey"), then a close spelling.
     */
    private fun match(name: String): CatalogItem? {
        val q = name.folded()
        if (q.isEmpty()) return null
        entries.firstOrNull { q == it.name || q == it.key }?.let { return it.item }
        val compactQuery = q.compact()
        entries.firstOrNull { compactQuery == it.compactName || compactQuery == it.compactKey }
            ?.let { return it.item }

        val longestInside = entries
            .mapNotNull { entry ->
                listOf(entry.name, entry.key)
                    .filter { q.hasWordStartingWith(it) }
                    .maxOfOrNull { it.length }
                    ?.let { entry to it }
            }
            .maxByOrNull { (_, length) -> length }
        if (longestInside != null) return longestInside.first.item

        val typos = typoTolerance(q)
        if (typos == 0) return null
        return entries
            .map { it to minOf(editDistance(q, it.name), editDistance(q, it.key)) }
            .filter { (_, distance) -> distance <= typos }
            .minByOrNull { (_, distance) -> distance }
            ?.first?.item
    }

    companion object {
        /** Suggestions start once this many letters are typed. */
        const val MIN_SUGGEST_LENGTH = 2

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

        /** Letters people often type without their accent, mapped to the plain letter. */
        private val plainLetters = mapOf(
            'ı' to 'i', 'ğ' to 'g', 'ş' to 's', 'ç' to 'c', 'ö' to 'o', 'ü' to 'u',
            'ä' to 'a', 'å' to 'a', 'é' to 'e', 'è' to 'e'
        )

        private const val COMBINING_DOT_ABOVE = '̇'

        /** Lowercase without accents, for comparing what was typed with a name. */
        private fun String.folded(): String =
            trim().lowercase(Locale.ROOT)
                .filterNot { it == COMBINING_DOT_ABOVE }
                .map { plainLetters[it] ?: it }
                .joinToString("")

        private fun String.compact(): String = filterNot { it.isWhitespace() }

        /** Typos forgiven for a query: none for short words, where guesses go wrong. */
        private fun typoTolerance(query: String): Int = when {
            query.length < 4 -> 0
            query.length < 7 -> 1
            else -> 2
        }

        /** Distance from [query] to the closest start of [name] (or the whole name). */
        private fun prefixDistance(query: String, name: String, typos: Int): Int =
            ((query.length - typos)..(query.length + typos))
                .filter { it in 1..name.length }
                .minOfOrNull { editDistance(query, name.take(it)) }
                ?: Int.MAX_VALUE

        /** Levenshtein distance: single-letter inserts, deletes and swaps. */
        private fun editDistance(a: String, b: String): Int {
            var previous = IntArray(b.length + 1) { it }
            for (i in 1..a.length) {
                val current = IntArray(b.length + 1)
                current[0] = i
                for (j in 1..b.length) {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    current[j] = minOf(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + cost)
                }
                previous = current
            }
            return previous[b.length]
        }

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
