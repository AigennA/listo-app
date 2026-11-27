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
    val unit: String? = null,
    /** When the item was ticked as bought; null if not ticked, or ticked before this was kept. */
    val checkedAt: Long? = null
)

/** A bought item joined with its category and list, for spending. */
data class BoughtItemRow(
    val name: String,
    val price: Double?,
    val amount: Double?,
    val unit: String?,
    val categoryName: String?,
    val categoryColor: String?,
    val storeId: Long?,
    val boughtAt: Long
) {
    fun toDomain() = com.listo.app.domain.spending.BoughtItem(
        name = name,
        lineTotal = price?.let {
            ShoppingItem(listId = 0, name = "", price = it, amount = amount, unit = com.listo.app.data.models.QuantityUnit.fromCode(unit)).lineTotal
        },
        categoryName = categoryName,
        categoryColor = categoryColor,
        storeId = storeId,
        boughtAt = boughtAt
    )
}

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
    unit = com.listo.app.data.models.QuantityUnit.fromCode(unit),
    checkedAt = checkedAt
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
    unit = unit?.code,
    checkedAt = checkedAt
)
