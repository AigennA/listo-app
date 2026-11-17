package com.listo.app.ui.viewmodel

import com.listo.app.data.models.ShoppingItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ListDetailUiStateTest {

    private fun item(
        id: Long,
        name: String,
        checked: Boolean = false,
        price: Double? = null,
        category: String? = null
    ) = ShoppingItem(
        id = id,
        listId = 1L,
        name = name,
        isChecked = checked,
        price = price,
        categoryName = category
    )

    @Test
    fun `computes counts, progress and total`() {
        val state = ListDetailUiState(
            items = listOf(
                item(1, "a", checked = true, price = 10.0, category = "Dairy"),
                item(2, "b", checked = false, price = 5.0, category = "Produce")
            ),
            isLoading = false
        )
        assertEquals(2, state.itemCount)
        assertEquals(1, state.checkedCount)
        assertEquals(0.5f, state.progress, 0.0001f)
        assertEquals(15.0, state.total, 0.0001)
    }

    @Test
    fun `grouped keeps uncategorised Other group last`() {
        val state = ListDetailUiState(
            items = listOf(
                item(1, "a", category = null),
                item(2, "b", category = "Dairy")
            ),
            isLoading = false
        )
        val keys = state.grouped.map { it.first }
        assertTrue(keys.contains("Dairy"))
        assertEquals("Other", keys.last())
    }

    @Test
    fun `checked items leave their category for the bought section`() {
        val state = ListDetailUiState(
            items = listOf(
                item(1, "milk", checked = true, category = "Dairy"),
                item(2, "cheese", checked = false, category = "Dairy"),
                item(3, "bread", checked = true, category = "Bakery")
            ),
            isLoading = false
        )
        assertEquals(listOf("Dairy" to listOf("cheese")), state.grouped.map { (k, v) -> k to v.map { it.name } })
        assertEquals(listOf("milk", "bread"), state.bought.map { it.name })
    }

    @Test
    fun `a category with everything bought disappears from the to-buy groups`() {
        val state = ListDetailUiState(
            items = listOf(item(1, "bread", checked = true, category = "Bakery")),
            isLoading = false
        )
        assertTrue(state.grouped.isEmpty())
        assertEquals(1, state.bought.size)
    }

    @Test
    fun `nothing bought means an empty bought section`() {
        val state = ListDetailUiState(items = listOf(item(1, "a")), isLoading = false)
        assertTrue(state.bought.isEmpty())
    }
}
