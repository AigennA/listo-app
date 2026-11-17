package com.listo.app.utils

import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.models.ShoppingList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ListShareTextTest {

    private val list = ShoppingList(id = 1, name = "Weekly")
    private val price: (Double) -> String = { "$it EUR" }

    @Test
    fun `lists every item with its box and quantity, then the total`() {
        val items = listOf(
            ShoppingItem(listId = 1, name = "Milk", isChecked = true, price = 1.5),
            ShoppingItem(listId = 1, name = "Eggs", quantity = "12", price = 3.0)
        )
        assertEquals(
            "Weekly\n\n[x] Milk\n[ ] Eggs (12)\n\n4.5 EUR",
            buildShareText(list, items, price)
        )
    }

    @Test
    fun `no prices means no total line`() {
        val text = buildShareText(list, listOf(ShoppingItem(listId = 1, name = "Milk")), price)
        assertFalse(text.contains("EUR"))
    }
}
