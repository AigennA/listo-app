package com.listo.app.domain.store

import com.listo.app.data.models.Category
import com.listo.app.data.models.StoreAisle
import com.listo.app.utils.GroceryCatalog

/** Colors for store icons, so every store the user adds looks different. */
object StoreColors {

    val palette: List<String> = listOf(
        "#E17055", "#0984E3", "#00B894", "#6C5CE7",
        "#E84393", "#F39C12", "#2D3436", "#D63031",
        "#00CEC9", "#A0522D"
    )

    /** The palette color used by the fewest stores, earliest first on a tie. */
    fun next(used: List<String>): String {
        val counts = used.groupingBy { it.uppercase() }.eachCount()
        return palette.minBy { counts[it.uppercase()] ?: 0 }
    }
}

/**
 * The grid a store map is drawn on. Aisles sit on whole cells; [COLUMNS] fits
 * a phone screen in portrait.
 */
object StoreGrid {
    const val COLUMNS = 6
    const val ROWS = 9

    fun contains(x: Int, y: Int): Boolean = x in 0 until COLUMNS && y in 0 until ROWS

    /** Every cell, row by row from the entrance at the top left. */
    val cells: List<Pair<Int, Int>> = (0 until ROWS).flatMap { y -> (0 until COLUMNS).map { x -> x to y } }

    /** Moves an aisle to a cell; an aisle already there swaps into its old place. */
    fun move(aisles: List<StoreAisle>, categoryId: Long, x: Int, y: Int): List<StoreAisle> {
        val moving = aisles.firstOrNull { it.categoryId == categoryId } ?: return aisles
        val tx = x.coerceIn(0, COLUMNS - 1)
        val ty = y.coerceIn(0, ROWS - 1)
        return aisles.map {
            when {
                it.categoryId == categoryId -> it.copy(x = tx, y = ty)
                it.x == tx && it.y == ty -> it.copy(x = moving.x, y = moving.y)
                else -> it
            }
        }
    }

    /** Puts a category on the first free cell, unless it is already on the map or the map is full. */
    fun add(aisles: List<StoreAisle>, categoryId: Long): List<StoreAisle> {
        if (aisles.any { it.categoryId == categoryId }) return aisles
        val taken = aisles.map { it.x to it.y }.toSet()
        val free = cells.firstOrNull { it !in taken } ?: return aisles
        return aisles + StoreAisle(categoryId = categoryId, x = free.first, y = free.second)
    }

    fun remove(aisles: List<StoreAisle>, categoryId: Long): List<StoreAisle> =
        aisles.filterNot { it.categoryId == categoryId }
}

/** The map a new store starts with, laid out like a typical supermarket walk. */
object DefaultStoreLayout {

    /** Built-in categories in the order most shoppers meet them. */
    private val walkOrder = listOf(
        GroceryCatalog.PRODUCE,
        GroceryCatalog.BAKERY,
        GroceryCatalog.MEAT,
        GroceryCatalog.DAIRY,
        GroceryCatalog.BREAKFAST,
        GroceryCatalog.PANTRY,
        GroceryCatalog.SNACKS,
        GroceryCatalog.DRINKS,
        GroceryCatalog.CLEANING
    )

    /** Aisle spots with a free cell around each, so the map reads like shelves. */
    private val spacedSlots: List<Pair<Int, Int>> =
        listOf(1, 3, 5, 7).flatMap { y -> listOf(0, 2, 4).map { x -> x to y } }

    fun layout(categories: List<Category>): List<StoreAisle> {
        val ordered = categories.sortedWith(
            compareBy<Category>({ walkOrder.indexOf(it.name).let { i -> if (i < 0) Int.MAX_VALUE else i } }, { it.name.lowercase() })
        )
        val slots = (spacedSlots + StoreGrid.cells.filterNot { it in spacedSlots })
        return ordered.zip(slots) { category, (x, y) -> StoreAisle(categoryId = category.id, x = x, y = y) }
    }
}
