package com.listo.app.data.database.dao

import androidx.room.*
import com.listo.app.data.database.entities.ShoppingListEntity
import com.listo.app.data.database.entities.ShoppingListWithStats
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Query(
        """
        SELECT l.*,
            (SELECT COUNT(*) FROM shopping_items i WHERE i.listId = l.id) AS itemCount,
            (SELECT COUNT(*) FROM shopping_items i WHERE i.listId = l.id AND i.isChecked = 1) AS checkedCount,
            (SELECT COALESCE(SUM(i.price), 0) FROM shopping_items i WHERE i.listId = l.id) AS totalPrice
        FROM shopping_lists l
        WHERE l.isArchived = 0
        ORDER BY l.createdAt DESC
        """
    )
    fun getActiveLists(): Flow<List<ShoppingListWithStats>>

    @Query(
        """
        SELECT l.*,
            (SELECT COUNT(*) FROM shopping_items i WHERE i.listId = l.id) AS itemCount,
            (SELECT COUNT(*) FROM shopping_items i WHERE i.listId = l.id AND i.isChecked = 1) AS checkedCount,
            (SELECT COALESCE(SUM(i.price), 0) FROM shopping_items i WHERE i.listId = l.id) AS totalPrice
        FROM shopping_lists l
        WHERE l.isArchived = 1
        ORDER BY l.archivedAt DESC
        """
    )
    fun getArchivedLists(): Flow<List<ShoppingListWithStats>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun observeListById(id: Long): Flow<ShoppingListEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ShoppingListEntity): Long

    @Update
    suspend fun update(list: ShoppingListEntity)

    @Query("UPDATE shopping_lists SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("UPDATE shopping_lists SET budget = :budget WHERE id = :id")
    suspend fun setBudget(id: Long, budget: Double?)

    @Query("UPDATE shopping_lists SET isArchived = :archived, archivedAt = :archivedAt WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean, archivedAt: Long?)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteById(id: Long)
}
