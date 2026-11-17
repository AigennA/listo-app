package com.listo.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GroceryCatalogTest {

    private val english = mapOf(
        GroceryCatalog.DAIRY to listOf("Milk", "Cheese", "Butter"),
        GroceryCatalog.PRODUCE to listOf("Tomato", "Apple"),
        GroceryCatalog.DRINKS to listOf("Tea", "Coffee")
    )

    // Stand-in translations: the catalog only cares that names differ from the keys.
    private val localized = mapOf(
        GroceryCatalog.DAIRY to listOf("Mlk-L", "Chs-L", "Btr-L"),
        GroceryCatalog.PRODUCE to listOf("Tmt-L", "Apl-L"),
        GroceryCatalog.DRINKS to listOf("Te", "Cof-L")
    )

    private val catalog = ItemCatalog.of(english, localized)

    @Test
    fun `suggest matches the localized names`() {
        assertEquals(listOf("Mlk-L"), catalog.suggest("mlk").map { it.name })
    }

    @Test
    fun `suggest with blank query is empty`() {
        assertTrue(catalog.suggest("   ").isEmpty())
    }

    @Test
    fun `suggest respects the limit`() {
        assertTrue(catalog.suggest("-l", limit = 2).size <= 2)
    }

    @Test
    fun `suggestions carry their category key`() {
        assertEquals(GroceryCatalog.DAIRY, catalog.suggest("chs").single().category)
    }

    @Test
    fun `categoryFor maps a localized name`() {
        assertEquals(GroceryCatalog.DAIRY, catalog.categoryFor("mlk-l"))
    }

    @Test
    fun `categoryFor still maps an english name`() {
        assertEquals(GroceryCatalog.DAIRY, catalog.categoryFor("Milk"))
    }

    @Test
    fun `categoryFor matches a known name at the start of a word`() {
        assertEquals(GroceryCatalog.PRODUCE, catalog.categoryFor("green apple"))
    }

    @Test
    fun `categoryFor ignores a short name hidden inside another word`() {
        // "Te" (tea) must not claim words that merely contain the letters "te".
        assertNull(catalog.categoryFor("pasted"))
    }

    @Test
    fun `categoryFor is null for an unknown item`() {
        assertNull(catalog.categoryFor("zzzznotathing"))
    }

    @Test
    fun `keyFor returns the english key of a localized name`() {
        assertEquals("Milk", catalog.keyFor("Mlk-L"))
    }

    @Test
    fun `keyFor is null for a free-typed name`() {
        assertNull(catalog.keyFor("Something else"))
    }

    @Test
    fun `itemsForCategory returns localized names`() {
        assertEquals(listOf("Tmt-L", "Apl-L"), catalog.itemsForCategory(GroceryCatalog.PRODUCE).map { it.name })
    }

    @Test
    fun `a category with a mismatched translation falls back to english`() {
        val broken = ItemCatalog.of(english, localized + (GroceryCatalog.PRODUCE to listOf("Only one")))
        assertEquals(listOf("Tomato", "Apple"), broken.itemsForCategory(GroceryCatalog.PRODUCE).map { it.name })
    }

    @Test
    fun `default categories are non-empty and unique`() {
        val names = GroceryCatalog.defaultCategories.map { it.first }
        assertTrue(names.isNotEmpty())
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun `every default category has an item list resource`() {
        GroceryCatalog.defaultCategories.forEach { (name, _) ->
            assertNotNull(name, GroceryCatalog.itemArrays[name])
        }
    }
}
