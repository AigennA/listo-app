package com.listo.app.data.models

import com.listo.app.domain.store.StoreGrid

/** A store with its own map. [color] is a hex color like "#E17055". */
data class Store(
    val id: Long = 0L,
    val name: String,
    val color: String,
    val entrance: GridCell = StoreLayout.DEFAULT_ENTRANCE,
    val checkout: GridCell = StoreLayout.DEFAULT_CHECKOUT
)

/** Where the aisle for [categoryId] sits on a store map, as a grid cell. */
data class StoreAisle(
    val categoryId: Long,
    val x: Int,
    val y: Int
) {
    val cell: GridCell get() = GridCell(x, y)
}

/** One cell of a store map. */
data class GridCell(val x: Int, val y: Int)

/**
 * Everything on a store map: the aisles, the entrance and the checkout. Each
 * sits on its own cell; dropping one onto another swaps the two.
 */
data class StoreLayout(
    val aisles: List<StoreAisle>,
    val entrance: GridCell = DEFAULT_ENTRANCE,
    val checkout: GridCell = DEFAULT_CHECKOUT
) {
    fun moveAisle(categoryId: Long, x: Int, y: Int): StoreLayout {
        val moving = aisles.firstOrNull { it.categoryId == categoryId } ?: return this
        return moveFrom(moving.cell, onGrid(x, y)).let { moved ->
            moved.copy(aisles = moved.aisles.map { if (it.categoryId == categoryId) it.copy(x = clampX(x), y = clampY(y)) else it })
        }
    }

    fun moveEntrance(x: Int, y: Int): StoreLayout =
        moveFrom(entrance, onGrid(x, y)).copy(entrance = onGrid(x, y))

    fun moveCheckout(x: Int, y: Int): StoreLayout =
        moveFrom(checkout, onGrid(x, y)).copy(checkout = onGrid(x, y))

    /** Puts a category on the first free cell, unless it is already there or the map is full. */
    fun addAisle(categoryId: Long): StoreLayout {
        if (aisles.any { it.categoryId == categoryId }) return this
        val taken = aisles.map { it.cell }.toSet() + entrance + checkout
        val free = StoreGrid.cells.firstOrNull { (x, y) -> GridCell(x, y) !in taken } ?: return this
        return copy(aisles = aisles + StoreAisle(categoryId, free.first, free.second))
    }

    fun removeAisle(categoryId: Long): StoreLayout = copy(aisles = aisles.filterNot { it.categoryId == categoryId })

    /** Whatever sits on [target] moves to [from], so a drop onto a taken cell swaps the two. */
    private fun moveFrom(from: GridCell, target: GridCell): StoreLayout = copy(
        aisles = aisles.map { if (it.cell == target) it.copy(x = from.x, y = from.y) else it },
        entrance = if (entrance == target) from else entrance,
        checkout = if (checkout == target) from else checkout
    )

    private fun clampX(x: Int) = x.coerceIn(0, StoreGrid.COLUMNS - 1)
    private fun clampY(y: Int) = y.coerceIn(0, StoreGrid.ROWS - 1)
    private fun onGrid(x: Int, y: Int) = GridCell(clampX(x), clampY(y))

    companion object {
        val DEFAULT_ENTRANCE = GridCell(0, 0)
        val DEFAULT_CHECKOUT = GridCell(StoreGrid.COLUMNS - 1, StoreGrid.ROWS - 1)
    }
}
