package com.listo.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GroceryCatalogTest {

    @Test
    fun `suggest returns only items containing the query`() {
        val results = GroceryCatalog.suggest("ch")
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.name.lowercase().contains("ch") })
    }

    @Test
    fun `suggest with blank query is empty`() {
        assertTrue(GroceryCatalog.suggest("   ").isEmpty())
    }

    @Test
    fun `suggest respects the limit`() {
        assertTrue(GroceryCatalog.suggest("e", limit = 3).size <= 3)
    }

    @Test
    fun `categoryFor maps a known item to its category`() {
        assertEquals(GroceryCatalog.DAIRY, GroceryCatalog.categoryFor("Milk"))
    }

    @Test
    fun `categoryFor is null for an unknown item`() {
        assertNull(GroceryCatalog.categoryFor("zzzznotathing"))
    }

    @Test
    fun `itemsForCategory returns that category's items`() {
        val produce = GroceryCatalog.itemsForCategory(GroceryCatalog.PRODUCE)
        assertTrue(produce.contains("Tomato"))
    }

    @Test
    fun `default categories are non-empty and unique`() {
        val names = GroceryCatalog.defaultCategories.map { it.first }
        assertTrue(names.isNotEmpty())
        assertEquals(names.size, names.toSet().size)
    }
}
