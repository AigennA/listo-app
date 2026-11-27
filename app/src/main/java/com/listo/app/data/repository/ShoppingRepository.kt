package com.listo.app.data.repository

import com.listo.app.data.database.dao.CategoryDao
import com.listo.app.data.database.dao.ShoppingItemDao
import com.listo.app.data.database.dao.ShoppingListDao
import com.listo.app.data.database.entities.ShoppingItemEntity
import com.listo.app.data.database.entities.toDomain
import com.listo.app.data.database.entities.toEntity
import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.models.ShoppingList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingRepository @Inject constructor(
    private val listDao: ShoppingListDao,
    private val itemDao: ShoppingItemDao,
    private val categoryDao: CategoryDao
) {

    // --- Lists ---

    fun getActiveLists(): Flow<List<ShoppingList>> =
        listDao.getActiveLists().map { rows -> rows.map { it.toDomain() } }

    fun getArchivedLists(): Flow<List<ShoppingList>> =
        listDao.getArchivedLists().map { rows -> rows.map { it.toDomain() } }

    fun observeListById(id: Long): Flow<ShoppingList?> =
        listDao.observeListById(id).map { entity -> entity?.toDomain() }

    suspend fun getListById(id: Long): ShoppingList? = listDao.getById(id)?.toDomain()

    suspend fun createList(name: String, scheduledDate: Long? = null, storeId: Long? = null): Long =
        listDao.insert(
            com.listo.app.data.database.entities.ShoppingListEntity(
                name = name,
                scheduledDate = scheduledDate,
                storeId = storeId
            )
        )

    suspend fun setStore(id: Long, storeId: Long?) = listDao.setStore(id, storeId)

    suspend fun renameList(id: Long, name: String) = listDao.rename(id, name)

    suspend fun setBudget(id: Long, budget: Double?) = listDao.setBudget(id, budget)

    suspend fun setSchedule(id: Long, scheduledDate: Long?, reminderAt: Long?) =
        listDao.setSchedule(id, scheduledDate, reminderAt)

    suspend fun getListsWithFutureReminders(now: Long): List<ShoppingList> =
        listDao.getListsWithFutureReminders(now).map { it.toDomain() }

    suspend fun archiveList(id: Long) =
        listDao.setArchived(id, archived = true, archivedAt = System.currentTimeMillis())

    suspend fun unarchiveList(id: Long) =
        listDao.setArchived(id, archived = false, archivedAt = null)

    suspend fun deleteList(id: Long) = listDao.deleteById(id)

    // --- Items ---

    fun getItemsForList(listId: Long): Flow<List<ShoppingItem>> =
        itemDao.getItemsForList(listId).map { entities ->
            entities.map { enrichWithCategory(it) }
        }

    suspend fun addItem(item: ShoppingItem): Long = itemDao.insert(item.toEntity())

    suspend fun updateItem(item: ShoppingItem) = itemDao.update(item.toEntity())

    suspend fun toggleItem(id: Long, checked: Boolean) = itemDao.setChecked(id, checked)

    suspend fun deleteItem(id: Long) = itemDao.deleteById(id)

    /** Count of unchecked items across all active (non-archived) lists. */
    suspend fun getActivePendingItemCount(): Int = itemDao.getActivePendingCount()

    private suspend fun enrichWithCategory(entity: ShoppingItemEntity): ShoppingItem {
        val category = entity.categoryId?.let { categoryDao.getCategoryById(it) }
        return entity.toDomain(
            categoryName = category?.name,
            categoryColor = category?.color
        )
    }
}
