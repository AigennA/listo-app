package com.listo.app.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ShoppingItemCopyTest {

    private val original = ShoppingItem(
        id = 7,
        listId = 1,
        name = "Milk",
        quantity = "2 L",
        categoryId = 3,
        price = 1.5,
        isChecked = true,
        createdAt = 100
    )

    @Test
    fun `a copied item belongs to the new list and is still to buy`() {
        val copy = original.copyTo(listId = 9, now = 500)
        assertEquals(9, copy.listId)
        assertFalse(copy.isChecked)
        assertEquals(0, copy.id)
        assertEquals(500, copy.createdAt)
    }

    @Test
    fun `a copied item keeps what it is`() {
        val copy = original.copyTo(listId = 9, now = 500)
        assertEquals(original.name, copy.name)
        assertEquals(original.quantity, copy.quantity)
        assertEquals(original.categoryId, copy.categoryId)
        assertEquals(original.price, copy.price)
    }
}
