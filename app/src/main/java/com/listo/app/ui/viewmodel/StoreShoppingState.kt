package com.listo.app.ui.viewmodel

import com.listo.app.data.models.Category
import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.models.StoreAisle

/** One aisle as drawn in shopping mode. */
data class AisleTile(
    val categoryId: Long,
    val name: String,
    val color: String,
    val x: Int,
    val y: Int,
    val remaining: Int,
    val total: Int
) {
    val hasItems: Boolean get() = total > 0

    /** Everything from this aisle is in the basket. */
    val isDone: Boolean get() = hasItems && remaining == 0
}

/**
 * A shopping list laid over a store map: what is left in each aisle, and the
 * items whose aisle is not on the map ([otherItems]), so nothing goes missing.
 */
data class StoreShoppingState(
    val aisles: List<StoreAisle> = emptyList(),
    val categories: List<Category> = emptyList(),
    val items: List<ShoppingItem> = emptyList()
) {
    val tiles: List<AisleTile>
        get() {
            val byId = categories.associateBy { it.id }
            return aisles.mapNotNull { aisle ->
                val category = byId[aisle.categoryId] ?: return@mapNotNull null
                val inAisle = itemsFor(aisle.categoryId)
                AisleTile(
                    categoryId = category.id,
                    name = category.name,
                    color = category.color,
                    x = aisle.x,
                    y = aisle.y,
                    remaining = inAisle.count { !it.isChecked },
                    total = inAisle.size
                )
            }
        }

    val otherItems: List<ShoppingItem>
        get() {
            val mapped = aisles.map { it.categoryId }.toSet()
            return items.filter { it.categoryId == null || it.categoryId !in mapped }
        }

    fun itemsFor(categoryId: Long): List<ShoppingItem> = items.filter { it.categoryId == categoryId }

    /**
     * Category of an item added in shopping mode: the aisle it was added in, or
     * for "not on the map" the category its name suggests ([guessedCategory]).
     */
    fun categoryForNewItem(aisleCategoryId: Long?, guessedCategory: String?): Long? =
        aisleCategoryId ?: guessedCategory?.let { name -> categories.firstOrNull { it.name == name }?.id }

    val checkedCount: Int get() = items.count { it.isChecked }
    val itemCount: Int get() = items.size
}
