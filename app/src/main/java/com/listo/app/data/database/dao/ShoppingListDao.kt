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
            (SELECT COALESCE(SUM(i.price * COALESCE(i.amount, 1) * CASE WHEN i.unit IN ('g', 'ml') THEN 0.001 ELSE 1 END), 0) FROM shopping_items i WHERE i.listId = l.id) AS totalPrice
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
            (SELECT COALESCE(SUM(i.price * COALESCE(i.amount, 1) * CASE WHEN i.unit IN ('g', 'ml') THEN 0.001 ELSE 1 END), 0) FROM shopping_items i WHERE i.listId = l.id) AS totalPrice
        FROM shopping_lists l
        WHERE l.isArchived = 1
        ORDER BY l.archivedAt DESC
        """
    )
    fun getArchivedLists(): Flow<List<ShoppingListWithStats>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun observeListById(id: Long): Flow<ShoppingListEntity?>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getById(id: Long): ShoppingListEntity?

    @Query("UPDATE shopping_lists SET scheduledDate = :scheduledDate, reminderAt = :reminderAt WHERE id = :id")
    suspend fun setSchedule(id: Long, scheduledDate: Long?, reminderAt: Long?)

    @Query("SELECT * FROM shopping_lists WHERE isArchived = 0 AND reminderAt IS NOT NULL AND reminderAt > :now")
    suspend fun getListsWithFutureReminders(now: Long): List<ShoppingListEntity>

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

    @Query("UPDATE shopping_lists SET storeId = :storeId WHERE id = :id")
    suspend fun setStore(id: Long, storeId: Long?)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteById(id: Long)
}
