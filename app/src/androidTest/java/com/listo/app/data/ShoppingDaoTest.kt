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
    fun tickingAnItem_remembersWhen_andUntickingForgets() = runBlocking {
        val listId = listDao.insert(ShoppingListEntity(name = "Groceries"))
        val id = itemDao.insert(ShoppingItemEntity(listId = listId, name = "Milk"))

        itemDao.setChecked(id, checked = true, now = 1_000L)
        assertEquals(1_000L, itemDao.getItemsForList(listId).first().single().checkedAt)

        itemDao.setChecked(id, checked = false, now = 2_000L)
        assertEquals(null, itemDao.getItemsForList(listId).first().single().checkedAt)
    }

    @Test
    fun boughtItems_carryCategoryStoreAndWhen() = runBlocking {
        val category = db.categoryDao().insertCategory(com.listo.app.data.database.entities.CategoryEntity(name = "Dairy", color = "#0984E3"))
        val store = db.storeDao().insertStore(com.listo.app.data.database.entities.StoreEntity(name = "Willys", color = "#E17055"))
        val listId = listDao.insert(ShoppingListEntity(name = "Weekly", storeId = store, isArchived = true, archivedAt = 5_000L))
        val ticked = itemDao.insert(ShoppingItemEntity(listId = listId, name = "Milk", price = 15.0, amount = 2.0, unit = "pcs", categoryId = category))
        itemDao.setChecked(ticked, checked = true, now = 3_000L)
        // Ticked before checkedAt existed: falls back to when the list was finished.
        itemDao.insert(ShoppingItemEntity(listId = listId, name = "Bread", price = 25.0, isChecked = true))
        itemDao.insert(ShoppingItemEntity(listId = listId, name = "Not bought", price = 99.0))

        val bought = itemDao.getBoughtItems().first().sortedBy { it.boughtAt }

        assertEquals(2, bought.size)
        assertEquals(3_000L, bought[0].boughtAt)
        assertEquals("Dairy", bought[0].categoryName)
        assertEquals(store, bought[0].storeId)
        assertEquals(30.0, bought[0].toDomain().lineTotal!!, 0.001)
        assertEquals(5_000L, bought[1].boughtAt)
    }

    @Test
    fun aisleTicks_comeFromListsShoppedInThatStore() = runBlocking {
        val produce = db.categoryDao().insertCategory(com.listo.app.data.database.entities.CategoryEntity(name = "Produce"))
        val willys = db.storeDao().insertStore(com.listo.app.data.database.entities.StoreEntity(name = "Willys", color = "#E17055"))
        val ica = db.storeDao().insertStore(com.listo.app.data.database.entities.StoreEntity(name = "ICA", color = "#0984E3"))
        val atWillys = listDao.insert(ShoppingListEntity(name = "A", storeId = willys))
        val atIca = listDao.insert(ShoppingListEntity(name = "B", storeId = ica))
        val tomato = itemDao.insert(ShoppingItemEntity(listId = atWillys, name = "Tomato", categoryId = produce))
        val apple = itemDao.insert(ShoppingItemEntity(listId = atIca, name = "Apple", categoryId = produce))
        itemDao.insert(ShoppingItemEntity(listId = atWillys, name = "No aisle"))
        itemDao.setChecked(tomato, checked = true, now = 10L)
        itemDao.setChecked(apple, checked = true, now = 20L)

        val ticks = itemDao.getAisleTicks(willys).first()

        assertEquals(1, ticks.size)
        assertEquals(produce, ticks.single().categoryId)
        assertEquals(10L, ticks.single().checkedAt)
    }

    @Test
    fun listTotal_multipliesPriceByAmount() = runBlocking {
        val listId = listDao.insert(ShoppingListEntity(name = "Groceries"))
        itemDao.insert(ShoppingItemEntity(listId = listId, name = "Milk", price = 15.0, amount = 2.0, unit = "pcs"))
        itemDao.insert(ShoppingItemEntity(listId = listId, name = "Cheese", price = 120.0, amount = 250.0, unit = "g"))
        itemDao.insert(ShoppingItemEntity(listId = listId, name = "Bread", price = 25.0))

        assertEquals(85.0, listDao.getActiveLists().first().single().totalPrice, 0.001)
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
