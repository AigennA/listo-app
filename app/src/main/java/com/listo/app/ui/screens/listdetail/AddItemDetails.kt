package com.listo.app.ui.screens.listdetail

/** An optional field of a new item, offered as a labelled button under the name. */
enum class ItemDetail { QUANTITY, PRICE, CATEGORY }

/**
 * Which optional fields of the add-item bar are open. The buttons appear once
 * a name is typed, so users see that quantity, price and category exist
 * without the bar being cluttered while idle.
 */
data class AddItemDetails(val open: Set<ItemDetail> = emptySet()) {

    fun isOpen(detail: ItemDetail): Boolean = detail in open

    fun toggle(detail: ItemDetail): AddItemDetails =
        copy(open = if (detail in open) open - detail else open + detail)

    fun showButtons(name: String): Boolean = name.isNotBlank() || open.isNotEmpty()
}
