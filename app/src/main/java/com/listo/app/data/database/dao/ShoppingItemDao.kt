package com.listo.app.data.database.dao

import androidx.room.*
import com.listo.app.data.database.entities.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY isChecked ASC, createdAt ASC")
    fun getItemsForList(listId: Long): Flow<List<ShoppingItemEntity>>

    @Query("SELECT COUNT(*) FROM shopping_items WHERE categoryId = :categoryId")
    fun getItemCountForCategory(categoryId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItemEntity): Long

    @Update
    suspend fun update(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET isChecked = :checked WHERE id = :id")
    suspend fun setChecked(id: Long, checked: Boolean)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
