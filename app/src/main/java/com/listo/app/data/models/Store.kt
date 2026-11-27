package com.listo.app.data.models

/** A store with its own aisle map. [color] is a hex color like "#E17055". */
data class Store(
    val id: Long = 0L,
    val name: String,
    val color: String
)

/** Where the aisle for [categoryId] sits on a store map, as a grid cell. */
data class StoreAisle(
    val categoryId: Long,
    val x: Int,
    val y: Int
)
