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

class StoreGridTest {

    private val produce = StoreAisle(categoryId = 1, x = 0, y = 1)
    private val dairy = StoreAisle(categoryId = 2, x = 2, y = 1)

    @Test
    fun `moving an aisle to a free cell puts it there`() {
        val moved = StoreGrid.move(listOf(produce, dairy), categoryId = 1, x = 4, y = 6)
        assertEquals(StoreAisle(1, 4, 6), moved.first { it.categoryId == 1L })
        assertEquals(dairy, moved.first { it.categoryId == 2L })
    }

    @Test
    fun `moving an aisle onto another swaps them`() {
        val moved = StoreGrid.move(listOf(produce, dairy), categoryId = 1, x = 2, y = 1)
        assertEquals(StoreAisle(1, 2, 1), moved.first { it.categoryId == 1L })
        assertEquals(StoreAisle(2, 0, 1), moved.first { it.categoryId == 2L })
    }

    @Test
    fun `moving outside the grid keeps the aisle on its edge`() {
        val moved = StoreGrid.move(listOf(produce), categoryId = 1, x = 99, y = -5)
        assertEquals(StoreAisle(1, StoreGrid.COLUMNS - 1, 0), moved.single())
    }

    @Test
    fun `adding a category places it on the first free cell`() {
        val added = StoreGrid.add(listOf(StoreAisle(1, 0, 0)), categoryId = 2)
        assertEquals(StoreAisle(2, 1, 0), added.first { it.categoryId == 2L })
    }

    @Test
    fun `adding a category that is already on the map changes nothing`() {
        val aisles = listOf(produce, dairy)
        assertEquals(aisles, StoreGrid.add(aisles, categoryId = 1))
    }

    @Test
    fun `adding to a full grid changes nothing`() {
        val full = (0 until StoreGrid.COLUMNS * StoreGrid.ROWS).map {
            StoreAisle(categoryId = it.toLong(), x = it % StoreGrid.COLUMNS, y = it / StoreGrid.COLUMNS)
        }
        assertEquals(full, StoreGrid.add(full, categoryId = 999))
    }

    @Test
    fun `removing a category takes its aisle off the map`() {
        assertEquals(listOf(dairy), StoreGrid.remove(listOf(produce, dairy), categoryId = 1))
    }
}
