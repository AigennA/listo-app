package com.listo.app.ui.viewmodel

import com.listo.app.data.models.Category
import com.listo.app.data.models.StoreAisle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreMapEditorStateTest {

    private val produce = Category(id = 1, name = "Produce")
    private val dairy = Category(id = 2, name = "Dairy")
    private val bakery = Category(id = 3, name = "Bakery")

    private val state = StoreMapEditorState(
        aisles = listOf(StoreAisle(1, 0, 1)),
        categories = listOf(produce, dairy, bakery)
    )

    @Test
    fun `categories not on the map are offered to add`() {
        assertEquals(listOf(2L, 3L), state.available.map { it.id })
    }

    @Test
    fun `placed aisles come with their category`() {
        val placed = state.placed.single()
        assertEquals(produce, placed.first)
        assertEquals(StoreAisle(1, 0, 1), placed.second)
    }

    @Test
    fun `an aisle whose category was deleted is dropped`() {
        val orphan = state.copy(aisles = state.aisles + StoreAisle(99, 3, 3))
        assertEquals(listOf(1L), orphan.placed.map { it.first.id })
    }

    @Test
    fun `changes are tracked until saved`() {
        assertFalse(state.hasChanges)
        assertTrue(state.copy(aisles = emptyList()).hasChanges)
    }
}
