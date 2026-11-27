package com.listo.app.domain.store

import com.listo.app.data.models.Category
import com.listo.app.data.models.StoreAisle
import com.listo.app.utils.GroceryCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreColorsTest {

    @Test
    fun `the first store gets the first palette color`() {
        assertEquals(StoreColors.palette.first(), StoreColors.next(emptyList()))
    }

    @Test
    fun `a new store gets a color no other store has`() {
        val used = StoreColors.palette.take(3)
        val next = StoreColors.next(used)
        assertFalse(next in used)
    }

    @Test
    fun `colors are compared case-insensitively`() {
        val used = listOf(StoreColors.palette.first().lowercase())
        assertNotEquals(StoreColors.palette.first(), StoreColors.next(used))
    }

    @Test
    fun `when every color is taken the least used one is reused`() {
        val used = StoreColors.palette + StoreColors.palette.drop(1)
        assertEquals(StoreColors.palette.first(), StoreColors.next(used))
    }

    @Test
    fun `the palette has distinct colors`() {
        assertEquals(StoreColors.palette.size, StoreColors.palette.map { it.uppercase() }.toSet().size)
        assertTrue(StoreColors.palette.size >= 8)
    }
}

class DefaultStoreLayoutTest {

    private fun category(id: Long, name: String) = Category(id = id, name = name)

    private val builtIn = listOf(
        category(1, GroceryCatalog.CLEANING),
        category(2, GroceryCatalog.DAIRY),
        category(3, GroceryCatalog.PRODUCE),
        category(4, GroceryCatalog.BAKERY)
    )

    @Test
    fun `every category gets one aisle`() {
        val layout = DefaultStoreLayout.layout(builtIn)
        assertEquals(builtIn.map { it.id }.toSet(), layout.map { it.categoryId }.toSet())
        assertEquals(builtIn.size, layout.size)
    }

    @Test
    fun `aisles follow a typical store walk, produce first and cleaning last`() {
        val order = DefaultStoreLayout.layout(builtIn).sortedWith(compareBy({ it.y }, { it.x })).map { it.categoryId }
        assertEquals(listOf(3L, 4L, 2L, 1L), order)
    }

    @Test
    fun `custom categories come after the built-in ones`() {
        val layout = DefaultStoreLayout.layout(builtIn + category(9, "Pet food"))
        val walk = layout.sortedWith(compareBy({ it.y }, { it.x })).map { it.categoryId }
        assertEquals(9L, walk.last())
    }

    @Test
    fun `aisles never share a cell and stay on the grid`() {
        val many = (1L..30L).map { category(it, "Custom $it") }
        val layout = DefaultStoreLayout.layout(many)
        assertEquals(layout.size, layout.map { it.x to it.y }.toSet().size)
        assertTrue(layout.all { StoreGrid.contains(it.x, it.y) })
    }

    @Test
    fun `no categories means an empty map`() {
        assertTrue(DefaultStoreLayout.layout(emptyList()).isEmpty())
    }
}

