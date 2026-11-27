package com.listo.app.ui.viewmodel

import com.listo.app.data.models.Category
import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.models.StoreAisle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreShoppingStateTest {

    private val produce = Category(id = 1, name = "Produce", color = "#2ECC71")
    private val dairy = Category(id = 2, name = "Dairy", color = "#0984E3")
    private val pets = Category(id = 3, name = "Pets", color = "#999999")

    private fun item(id: Long, categoryId: Long?, checked: Boolean = false) =
        ShoppingItem(id = id, listId = 1, name = "item$id", categoryId = categoryId, isChecked = checked)

    private val state = StoreShoppingState(
        aisles = listOf(StoreAisle(1, x = 0, y = 1), StoreAisle(2, x = 2, y = 1)),
        categories = listOf(produce, dairy, pets),
        items = listOf(
            item(1, categoryId = 1),
            item(2, categoryId = 1, checked = true),
            item(3, categoryId = 2, checked = true),
            item(4, categoryId = 3),
            item(5, categoryId = null)
        )
    )

    @Test
    fun `each aisle shows how many of its items are still to buy`() {
        val tiles = state.tiles.associateBy { it.categoryId }
        assertEquals(1, tiles.getValue(1).remaining)
        assertEquals(2, tiles.getValue(1).total)
        assertEquals(0, tiles.getValue(2).remaining)
    }

    @Test
    fun `an aisle with everything bought is done`() {
        val tiles = state.tiles.associateBy { it.categoryId }
        assertTrue(tiles.getValue(2).isDone)
        assertFalse(tiles.getValue(1).isDone)
    }

    @Test
    fun `an aisle with nothing on the list is quiet, not done`() {
        val empty = state.copy(items = emptyList()).tiles.first()
        assertEquals(0, empty.total)
        assertFalse(empty.isDone)
        assertFalse(empty.hasItems)
    }

    @Test
    fun `tiles carry the category name, color and position`() {
        val tile = state.tiles.first { it.categoryId == 2L }
        assertEquals("Dairy", tile.name)
        assertEquals("#0984E3", tile.color)
        assertEquals(2 to 1, tile.x to tile.y)
    }

    @Test
    fun `items whose aisle is not on the map are kept in Other`() {
        assertEquals(listOf(4L, 5L), state.otherItems.map { it.id })
    }

    @Test
    fun `tapping an aisle lists only its items`() {
        assertEquals(listOf(1L, 2L), state.itemsFor(1).map { it.id })
    }

    @Test
    fun `overall progress counts every item on the list`() {
        assertEquals(2, state.checkedCount)
        assertEquals(5, state.itemCount)
    }

    @Test
    fun `an item added in an aisle goes into that aisle`() {
        assertEquals(2L, state.categoryForNewItem(aisleCategoryId = 2, guessedCategory = "Produce"))
    }

    @Test
    fun `an item added under Other gets the category its name suggests`() {
        assertEquals(1L, state.categoryForNewItem(aisleCategoryId = null, guessedCategory = "Produce"))
    }

    @Test
    fun `an unknown item added under Other has no category`() {
        assertEquals(null, state.categoryForNewItem(aisleCategoryId = null, guessedCategory = null))
        assertEquals(null, state.categoryForNewItem(aisleCategoryId = null, guessedCategory = "Nope"))
    }

    @Test
    fun `an aisle for a deleted category is not drawn`() {
        val orphan = state.copy(aisles = state.aisles + StoreAisle(99, x = 5, y = 5))
        assertFalse(orphan.tiles.any { it.categoryId == 99L })
    }
}
