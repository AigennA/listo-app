package com.listo.app.data.models

data class ShoppingItem(
    val id: Long = 0L,
    val listId: Long,
    val name: String,
    val quantity: String = "",
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val categoryColor: String? = null,
    val price: Double? = null,
    val isChecked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** This item as a new, unticked row of list [listId] (used when copying a list). */
    fun copyTo(listId: Long, now: Long = System.currentTimeMillis()): ShoppingItem =
        copy(id = 0L, listId = listId, isChecked = false, createdAt = now)
}
