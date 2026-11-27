package com.listo.app.data.models

import com.listo.app.utils.QuantityStepper

data class ShoppingItem(
    val id: Long = 0L,
    val listId: Long,
    val name: String,
    /** Free-text quantity from before amounts and units existed, e.g. "2 kg". */
    val quantity: String = "",
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val categoryColor: String? = null,
    /** Price per piece, pack, kilo or litre (see [QuantityUnit]). */
    val price: Double? = null,
    val isChecked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val amount: Double? = null,
    val unit: QuantityUnit? = null
) {
    /** What this item adds to the list total: price times amount; once if no amount is set. */
    val lineTotal: Double
        get() = (price ?: 0.0) * (amount ?: 1.0) * (unit?.priceFactor ?: 1.0)

    /**
     * One −/+ step on the amount. An item without an amount starts counting
     * pieces; the amount then replaces any old free-text quantity.
     */
    fun stepped(up: Boolean): ShoppingItem {
        val u = unit ?: QuantityUnit.PIECE
        val next = if (up) QuantityStepper.up(amount, u) else QuantityStepper.down(amount, u)
        return copy(amount = next, unit = u, quantity = "")
    }

    /** This item as a new, unticked row of list [listId] (used when copying a list). */
    fun copyTo(listId: Long, now: Long = System.currentTimeMillis()): ShoppingItem =
        copy(id = 0L, listId = listId, isChecked = false, createdAt = now)
}
