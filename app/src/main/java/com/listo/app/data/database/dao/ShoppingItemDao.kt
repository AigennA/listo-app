package com.listo.app.data.database.dao

import androidx.room.*
import com.listo.app.data.database.entities.BoughtItemRow
import com.listo.app.domain.store.AisleTick
import com.listo.app.data.database.entities.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY isChecked ASC, createdAt ASC")
    fun getItemsForList(listId: Long): Flow<List<ShoppingItemEntity>>

    @Query("SELECT COUNT(*) FROM shopping_items WHERE categoryId = :categoryId")
    fun getItemCountForCategory(categoryId: Long): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM shopping_items
        WHERE isChecked = 0
          AND listId IN (SELECT id FROM shopping_lists WHERE isArchived = 0)
        """
    )
    suspend fun getActivePendingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItemEntity): Long

    @Update
    suspend fun update(item: ShoppingItemEntity)

    /** Ticks or unticks an item; a tick remembers [now] as when it was bought. */
    @Query(
        "UPDATE shopping_items SET isChecked = :checked, " +
            "checkedAt = CASE WHEN :checked THEN :now ELSE NULL END WHERE id = :id"
    )
    suspend fun setChecked(id: Long, checked: Boolean, now: Long)

    /**
     * Every bought item with its category and store. Items ticked before tick
     * times were kept count from when their list was finished, else when added.
     */
    @Query(
        """
        SELECT i.name AS name, i.price AS price, i.amount AS amount, i.unit AS unit,
            c.name AS categoryName, c.color AS categoryColor, l.storeId AS storeId,
            COALESCE(i.checkedAt, l.archivedAt, i.createdAt) AS boughtAt
        FROM shopping_items i
        JOIN shopping_lists l ON l.id = i.listId
        LEFT JOIN categories c ON c.id = i.categoryId
        WHERE i.isChecked = 1
        """
    )
    fun getBoughtItems(): Flow<List<BoughtItemRow>>

    /** Ticked items with an aisle, from lists shopped in [storeId], to learn the walking order. */
    @Query(
        """
        SELECT i.listId AS listId, i.categoryId AS categoryId, i.checkedAt AS checkedAt
        FROM shopping_items i
        JOIN shopping_lists l ON l.id = i.listId
        WHERE l.storeId = :storeId AND i.checkedAt IS NOT NULL AND i.categoryId IS NOT NULL
        """
    )
    fun getAisleTicks(storeId: Long): Flow<List<AisleTick>>

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
