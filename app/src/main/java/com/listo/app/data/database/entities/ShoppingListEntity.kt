package com.listo.app.data.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.listo.app.data.models.ShoppingList

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val archivedAt: Long? = null,
    val budget: Double? = null,
    val scheduledDate: Long? = null,
    val reminderAt: Long? = null,
    /** The store this list is usually shopped in, or null. */
    val storeId: Long? = null
)

/** Projection used by list queries to carry aggregate counts/total alongside the row. */
data class ShoppingListWithStats(
    @Embedded val list: ShoppingListEntity,
    val itemCount: Int,
    val checkedCount: Int,
    val totalPrice: Double
)

fun ShoppingListWithStats.toDomain() = ShoppingList(
    id = list.id,
    name = list.name,
    createdAt = list.createdAt,
    isArchived = list.isArchived,
    archivedAt = list.archivedAt,
    budget = list.budget,
    scheduledDate = list.scheduledDate,
    reminderAt = list.reminderAt,
    storeId = list.storeId,
    itemCount = itemCount,
    checkedCount = checkedCount,
    totalPrice = totalPrice
)

fun ShoppingListEntity.toDomain() = ShoppingList(
    id = id,
    name = name,
    createdAt = createdAt,
    isArchived = isArchived,
    archivedAt = archivedAt,
    budget = budget,
    scheduledDate = scheduledDate,
    reminderAt = reminderAt,
    storeId = storeId
)

fun ShoppingList.toEntity() = ShoppingListEntity(
    id = id,
    name = name,
    createdAt = createdAt,
    isArchived = isArchived,
    archivedAt = archivedAt,
    budget = budget,
    scheduledDate = scheduledDate,
    reminderAt = reminderAt,
    storeId = storeId
)
