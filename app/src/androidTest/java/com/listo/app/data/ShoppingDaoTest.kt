package com.listo.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.listo.app.data.database.LisToDatabase
import com.listo.app.data.database.dao.ShoppingItemDao
import com.listo.app.data.database.dao.ShoppingListDao
import com.listo.app.data.database.entities.ShoppingItemEntity
import com.listo.app.data.database.entities.ShoppingListEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Behaviour tests for the shopping persistence layer against a real (in-memory) Room db. */
@RunWith(AndroidJUnit4::class)
class ShoppingDaoTest {

    private lateinit var db: LisToDatabase
    private lateinit var listDao: ShoppingListDao
    private lateinit var itemDao: ShoppingItemDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LisToDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        listDao = db.shoppingListDao()
        itemDao = db.shoppingItemDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun activeList_aggregatesItemCountsAndTotal() = runBlocking {
        val listId = listDao.insert(ShoppingListEntity(name = "Groceries"))
        itemDao.insert(ShoppingItemEntity(listId = listId, name = "Milk", price = 10.0, isChecked = true))
        itemDao.insert(ShoppingItemEntity(listId = listId, name = "Bread", price = 5.0, isChecked = false))

        val stats = listDao.getActiveLists().first().single()
        assertEquals(2, stats.itemCount)
        assertEquals(1, stats.checkedCount)
        assertEquals(15.0, stats.totalPrice, 0.0001)
    }

    @Test
    fun pendingCount_countsOnlyUncheckedItemsInActiveLists() = runBlocking {
        val active = listDao.insert(ShoppingListEntity(name = "Active"))
        itemDao.insert(ShoppingItemEntity(listId = active, name = "x", isChecked = false))
        itemDao.insert(ShoppingItemEntity(listId = active, name = "y", isChecked = true))

        val archived = listDao.insert(ShoppingListEntity(name = "Archived", isArchived = true))
        itemDao.insert(ShoppingItemEntity(listId = archived, name = "z", isChecked = false))

        assertEquals(1, itemDao.getActivePendingCount())
    }

    @Test
    fun archiving_movesListFromActiveToArchived() = runBlocking {
        val id = listDao.insert(ShoppingListEntity(name = "Trip"))
        assertEquals(1, listDao.getActiveLists().first().size)

        listDao.setArchived(id, archived = true, archivedAt = 123L)

        assertTrue(listDao.getActiveLists().first().isEmpty())
        assertEquals(1, listDao.getArchivedLists().first().size)
    }

    @Test
    fun deletingList_cascadesToItems() = runBlocking {
        val id = listDao.insert(ShoppingListEntity(name = "Trip"))
        itemDao.insert(ShoppingItemEntity(listId = id, name = "x"))

        listDao.deleteById(id)

        assertTrue(itemDao.getItemsForList(id).first().isEmpty())
    }
}
