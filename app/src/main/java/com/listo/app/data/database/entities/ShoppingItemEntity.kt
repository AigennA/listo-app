package com.listo.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.listo.app.data.models.ShoppingItem

@Entity(
    tableName = "shopping_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("listId"), Index("categoryId")]
)
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val listId: Long,
    val name: String,
    val quantity: String = "",
    val categoryId: Long? = null,
    val price: Double? = null,
    val isChecked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val amount: Double? = null,
    /** [com.listo.app.data.models.QuantityUnit.code], or null. */
    val unit: String? = null
)

fun ShoppingItemEntity.toDomain(
    categoryName: String? = null,
    categoryColor: String? = null
) = ShoppingItem(
    id = id,
    listId = listId,
    name = name,
    quantity = quantity,
    categoryId = categoryId,
    categoryName = categoryName,
    categoryColor = categoryColor,
    price = price,
    isChecked = isChecked,
    createdAt = createdAt,
    amount = amount,
    unit = com.listo.app.data.models.QuantityUnit.fromCode(unit)
)

fun ShoppingItem.toEntity() = ShoppingItemEntity(
    id = id,
    listId = listId,
    name = name,
    quantity = quantity,
    categoryId = categoryId,
    price = price,
    isChecked = isChecked,
    createdAt = createdAt,
    amount = amount,
    unit = unit?.code
)
