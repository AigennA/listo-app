package com.listo.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.listo.app.data.database.LisToDatabase
import com.listo.app.data.database.entities.CategoryEntity
import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** End-to-end behaviour of the shopping repository against a real (in-memory) db. */
@RunWith(AndroidJUnit4::class)
class ShoppingRepositoryBehaviourTest {

    private lateinit var db: LisToDatabase
    private lateinit var repository: ShoppingRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LisToDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShoppingRepository(db.shoppingListDao(), db.shoppingItemDao(), db.categoryDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun addedItem_isEnrichedWithCategoryNameAndColor() = runBlocking {
        val categoryId = db.categoryDao().insertCategory(CategoryEntity(name = "Dairy", color = "#0984E3"))
        val listId = repository.createList("Groceries")

        repository.addItem(ShoppingItem(listId = listId, name = "Milk", categoryId = categoryId))

        val item = repository.getItemsForList(listId).first().single()
        assertEquals("Dairy", item.categoryName)
        assertEquals("#0984E3", item.categoryColor)
    }

    @Test
    fun togglingItem_updatesCheckedState() = runBlocking {
        val listId = repository.createList("Groceries")
        val itemId = repository.addItem(ShoppingItem(listId = listId, name = "Eggs"))

        repository.toggleItem(itemId, checked = true)

        assertTrue(repository.getItemsForList(listId).first().single().isChecked)
    }

    @Test
    fun activeListStats_reflectAddedItems() = runBlocking {
        val listId = repository.createList("Groceries")
        repository.addItem(ShoppingItem(listId = listId, name = "a", price = 10.0, isChecked = true))
        repository.addItem(ShoppingItem(listId = listId, name = "b", price = 5.0))

        val list = repository.getActiveLists().first().single()
        assertEquals(2, list.itemCount)
        assertEquals(1, list.checkedCount)
        assertEquals(15.0, list.totalPrice, 0.0001)
    }

    @Test
    fun settingBudget_isObservedOnTheList() = runBlocking {
        val listId = repository.createList("Groceries")
        repository.setBudget(listId, 250.0)

        assertEquals(250.0, repository.observeListById(listId).first()?.budget!!, 0.0001)

        repository.setBudget(listId, null)
        assertNull(repository.observeListById(listId).first()?.budget)
    }

    @Test
    fun archivingList_movesItOutOfActiveLists() = runBlocking {
        val listId = repository.createList("Groceries")
        repository.addItem(ShoppingItem(listId = listId, name = "a", isChecked = false))

        assertEquals(1, repository.getActivePendingItemCount())

        repository.archiveList(listId)

        assertTrue(repository.getActiveLists().first().isEmpty())
        assertEquals(1, repository.getArchivedLists().first().size)
        assertEquals(0, repository.getActivePendingItemCount())
    }
}
