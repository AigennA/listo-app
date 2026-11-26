package com.listo.app.ui.screens.listdetail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddItemDetailsTest {

    @Test
    fun `nothing is open at first`() {
        assertTrue(AddItemDetails().open.isEmpty())
    }

    @Test
    fun `the detail buttons stay hidden until a name is typed`() {
        assertFalse(AddItemDetails().showButtons(name = ""))
        assertFalse(AddItemDetails().showButtons(name = "   "))
        assertTrue(AddItemDetails().showButtons(name = "Milk"))
    }

    @Test
    fun `an open detail keeps the buttons visible even with no name`() {
        val details = AddItemDetails().toggle(ItemDetail.PRICE)
        assertTrue(details.showButtons(name = ""))
    }

    @Test
    fun `tapping a button opens only that field`() {
        val details = AddItemDetails().toggle(ItemDetail.PRICE)
        assertEquals(setOf(ItemDetail.PRICE), details.open)
        assertTrue(details.isOpen(ItemDetail.PRICE))
        assertFalse(details.isOpen(ItemDetail.QUANTITY))
    }

    @Test
    fun `tapping an open button closes it`() {
        val details = AddItemDetails().toggle(ItemDetail.QUANTITY).toggle(ItemDetail.QUANTITY)
        assertTrue(details.open.isEmpty())
    }

    @Test
    fun `several fields can be open together`() {
        val details = AddItemDetails().toggle(ItemDetail.QUANTITY).toggle(ItemDetail.CATEGORY)
        assertEquals(setOf(ItemDetail.QUANTITY, ItemDetail.CATEGORY), details.open)
    }

    @Test
    fun `buttons follow a fixed order`() {
        assertEquals(
            listOf(ItemDetail.QUANTITY, ItemDetail.PRICE, ItemDetail.CATEGORY),
            ItemDetail.entries.toList()
        )
    }
}
