package com.listo.app.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingListTest {

    @Test
    fun `progress is zero with no items`() {
        assertEquals(0f, ShoppingList(name = "x").progress, 0f)
    }

    @Test
    fun `progress reflects checked ratio`() {
        val list = ShoppingList(name = "x", itemCount = 4, checkedCount = 2)
        assertEquals(0.5f, list.progress, 0.0001f)
    }

    @Test
    fun `isComplete only when all items checked`() {
        assertTrue(ShoppingList(name = "x", itemCount = 3, checkedCount = 3).isComplete)
        assertFalse(ShoppingList(name = "x", itemCount = 3, checkedCount = 2).isComplete)
        assertFalse(ShoppingList(name = "x", itemCount = 0, checkedCount = 0).isComplete)
    }

    @Test
    fun `isOverBudget only when total exceeds a set budget`() {
        assertTrue(ShoppingList(name = "x", budget = 10.0, totalPrice = 12.0).isOverBudget)
        assertFalse(ShoppingList(name = "x", budget = 10.0, totalPrice = 8.0).isOverBudget)
        assertFalse(ShoppingList(name = "x", budget = null, totalPrice = 999.0).isOverBudget)
    }
}
