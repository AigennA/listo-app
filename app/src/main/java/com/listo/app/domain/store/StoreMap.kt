package com.listo.app.domain.store

import com.listo.app.data.models.Category
import com.listo.app.data.models.GridCell
import com.listo.app.data.models.StoreAisle
import com.listo.app.data.models.StoreLayout
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
        // The entrance and checkout keep their default cells.
        val reserved = setOf(StoreLayout.DEFAULT_ENTRANCE, StoreLayout.DEFAULT_CHECKOUT).map { it.x to it.y }
        val slots = (spacedSlots + StoreGrid.cells.filterNot { it in spacedSlots }).filterNot { it in reserved }
        return ordered.zip(slots) { category, (x, y) -> StoreAisle(categoryId = category.id, x = x, y = y) }
    }
}

/** Which way an arrow points on the map, with its rotation from "right". */
enum class Direction(val degrees: Float) {
    RIGHT(0f), DOWN(90f), LEFT(180f), UP(270f)
}

/**
 * The way into the store from an entrance on [cell]: away from the nearest
 * wall. In a corner the side wall wins.
 */
fun entranceDirection(cell: GridCell): Direction = listOf(
    cell.x to Direction.RIGHT,
    StoreGrid.COLUMNS - 1 - cell.x to Direction.LEFT,
    cell.y to Direction.DOWN,
    StoreGrid.ROWS - 1 - cell.y to Direction.UP
).minBy { (distance, _) -> distance }.second
