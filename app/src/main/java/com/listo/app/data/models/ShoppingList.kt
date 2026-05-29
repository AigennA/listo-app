package com.listo.app.data.models

data class ShoppingList(
    val id: Long = 0L,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val archivedAt: Long? = null,
    val itemCount: Int = 0,
    val checkedCount: Int = 0,
    val totalPrice: Double = 0.0
) {
    val progress: Float
        get() = if (itemCount == 0) 0f else checkedCount.toFloat() / itemCount

    val isComplete: Boolean
        get() = itemCount > 0 && checkedCount == itemCount
}
