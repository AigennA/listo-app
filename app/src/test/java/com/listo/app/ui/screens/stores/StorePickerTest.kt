package com.listo.app.ui.screens.stores

import com.listo.app.data.models.Store
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorePickerTest {

    private val stores = listOf(
        Store(id = 1, name = "City Market North", color = "#E17055"),
        Store(id = 2, name = "Big Basket North", color = "#0984E3"),
        Store(id = 3, name = "Fresh Corner", color = "#00B894")
    )

    @Test
    fun `an empty query shows every store`() {
        assertEquals(stores, StorePicker.matches(stores, ""))
    }

    @Test
    fun `typing filters stores by any part of the name`() {
        assertEquals(listOf(1L, 2L), StorePicker.matches(stores, "north").map { it.id })
    }

    @Test
    fun `a name not in My stores can be added`() {
        assertTrue(StorePicker.canAdd(stores, "Green Grocer East"))
    }

    @Test
    fun `an existing name or a blank one cannot be added again`() {
        assertFalse(StorePicker.canAdd(stores, " fresh corner "))
        assertFalse(StorePicker.canAdd(stores, "   "))
    }
}
