package com.listo.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.listo.app.data.models.Store
import com.listo.app.data.models.StoreAisle

/** A store the user shops at, e.g. "Willys Tornby", shown in its own [color]. */
@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val color: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * One aisle on a store's map: where a category sits, as a cell ([x], [y]) on
 * the map grid. A category appears at most once per store.
 */
@Entity(
    tableName = "store_aisles",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["storeId", "categoryId"], unique = true), Index("categoryId")]
)
data class StoreAisleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val storeId: Long,
    val categoryId: Long,
    val x: Int,
    val y: Int
)

fun StoreEntity.toDomain() = Store(id = id, name = name, color = color)

fun StoreAisleEntity.toDomain() = StoreAisle(categoryId = categoryId, x = x, y = y)

fun StoreAisle.toEntity(storeId: Long) =
    StoreAisleEntity(storeId = storeId, categoryId = categoryId, x = x, y = y)
