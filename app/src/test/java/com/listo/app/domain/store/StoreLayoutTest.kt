package com.listo.app.domain.store

import com.listo.app.data.models.Category
import com.listo.app.data.models.GridCell
import com.listo.app.data.models.StoreAisle
import com.listo.app.data.models.StoreLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreLayoutTest {

    private val layout = StoreLayout(
        aisles = listOf(StoreAisle(1, x = 0, y = 1), StoreAisle(2, x = 2, y = 1)),
        entrance = GridCell(0, 0),
        checkout = GridCell(5, 8)
    )

    @Test
    fun `a new store has its entrance top left and checkout bottom right`() {
        assertEquals(GridCell(0, 0), StoreLayout.DEFAULT_ENTRANCE)
        assertEquals(GridCell(StoreGrid.COLUMNS - 1, StoreGrid.ROWS - 1), StoreLayout.DEFAULT_CHECKOUT)
    }

    @Test
    fun `the database defaults match the default entrance and checkout`() {
        // StoreEntity's column defaults are literals; keep them in step with the model.
        val entity = com.listo.app.data.database.entities.StoreEntity(name = "x", color = "#000000")
        assertEquals(StoreLayout.DEFAULT_ENTRANCE, GridCell(entity.entranceX, entity.entranceY))
        assertEquals(StoreLayout.DEFAULT_CHECKOUT, GridCell(entity.checkoutX, entity.checkoutY))
        assertEquals(GridCell(5, 8), StoreLayout.DEFAULT_CHECKOUT)
    }

    @Test
    fun `the entrance moves to a free cell`() {
        assertEquals(GridCell(3, 0), layout.moveEntrance(3, 0).entrance)
    }

    @Test
    fun `the entrance dropped on an aisle swaps places with it`() {
        val moved = layout.moveEntrance(2, 1)
        assertEquals(GridCell(2, 1), moved.entrance)
        assertEquals(StoreAisle(2, x = 0, y = 0), moved.aisles.first { it.categoryId == 2L })
    }

    @Test
    fun `the checkout dropped on the entrance swaps with it`() {
        val moved = layout.moveCheckout(0, 0)
        assertEquals(GridCell(0, 0), moved.checkout)
        assertEquals(GridCell(5, 8), moved.entrance)
    }

    @Test
    fun `an aisle dropped on the checkout swaps with it`() {
        val moved = layout.moveAisle(1, 5, 8)
        assertEquals(StoreAisle(1, x = 5, y = 8), moved.aisles.first { it.categoryId == 1L })
        assertEquals(GridCell(0, 1), moved.checkout)
    }

    @Test
    fun `moving stays on the grid`() {
        assertEquals(GridCell(StoreGrid.COLUMNS - 1, 0), layout.moveEntrance(99, -3).entrance)
    }

    @Test
    fun `a new aisle never lands on the entrance or checkout`() {
        val full = StoreLayout(aisles = emptyList(), entrance = GridCell(0, 0), checkout = GridCell(1, 0))
        val added = full.addAisle(7)
        assertEquals(StoreAisle(7, x = 2, y = 0), added.aisles.single())
    }

    @Test
    fun `the default map keeps the entrance and checkout cells free`() {
        val many = (1L..60L).map { Category(id = it, name = "Custom $it") }
        val aisles = DefaultStoreLayout.layout(many)
        val cells = aisles.map { GridCell(it.x, it.y) }
        assertFalse(StoreLayout.DEFAULT_ENTRANCE in cells)
        assertFalse(StoreLayout.DEFAULT_CHECKOUT in cells)
        assertEquals(cells.size, cells.toSet().size)
    }

    @Test
    fun `adding a category already on the map changes nothing`() {
        assertEquals(layout, layout.addAisle(1))
    }

    @Test
    fun `adding to a full map changes nothing`() {
        val cells = StoreGrid.cells.drop(2).mapIndexed { i, (x, y) -> StoreAisle(i.toLong() + 10, x, y) }
        val full = StoreLayout(cells, entrance = GridCell(0, 0), checkout = GridCell(1, 0))
        assertEquals(full, full.addAisle(999))
    }

    @Test
    fun `moving an aisle to a free cell leaves the others alone`() {
        val moved = layout.moveAisle(1, 4, 6)
        assertEquals(StoreAisle(1, 4, 6), moved.aisles.first { it.categoryId == 1L })
        assertEquals(StoreAisle(2, 2, 1), moved.aisles.first { it.categoryId == 2L })
        assertEquals(layout.entrance, moved.entrance)
    }

    @Test
    fun `removing an aisle keeps the entrance and checkout`() {
        val removed = layout.removeAisle(1)
        assertEquals(listOf(2L), removed.aisles.map { it.categoryId })
        assertEquals(layout.entrance, removed.entrance)
    }
}

class EntranceArrowTest {

    @Test
    fun `the arrow points into the store from the nearest wall`() {
        assertEquals(Direction.RIGHT, entranceDirection(GridCell(0, 4)))
        assertEquals(Direction.LEFT, entranceDirection(GridCell(StoreGrid.COLUMNS - 1, 4)))
        assertEquals(Direction.DOWN, entranceDirection(GridCell(3, 0)))
        assertEquals(Direction.UP, entranceDirection(GridCell(3, StoreGrid.ROWS - 1)))
    }

    @Test
    fun `in a corner the side wall wins`() {
        assertEquals(Direction.RIGHT, entranceDirection(GridCell(0, 0)))
    }

    @Test
    fun `every direction has a rotation for the arrow`() {
        assertTrue(Direction.entries.map { it.degrees }.toSet().size == Direction.entries.size)
    }
}
