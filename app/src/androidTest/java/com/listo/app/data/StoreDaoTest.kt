package com.listo.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.listo.app.data.database.LisToDatabase
import com.listo.app.data.database.dao.StoreDao
import com.listo.app.data.database.entities.CategoryEntity
import com.listo.app.data.database.entities.ShoppingListEntity
import com.listo.app.data.database.entities.StoreAisleEntity
import com.listo.app.data.database.entities.StoreEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Behaviour tests for stores and their aisle maps against a real (in-memory) Room db. */
@RunWith(AndroidJUnit4::class)
class StoreDaoTest {

    private lateinit var db: LisToDatabase
    private lateinit var storeDao: StoreDao
    private var produce = 0L
    private var dairy = 0L

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LisToDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        storeDao = db.storeDao()
        produce = db.categoryDao().insertCategory(CategoryEntity(name = "Produce"))
        dairy = db.categoryDao().insertCategory(CategoryEntity(name = "Dairy"))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun stores_areListedByName() = runBlocking {
        storeDao.insertStore(StoreEntity(name = "City Market North", color = "#E17055"))
        storeDao.insertStore(StoreEntity(name = "Big Basket Mall", color = "#0984E3"))

        assertEquals(
            listOf("Big Basket Mall", "City Market North"),
            storeDao.observeStores().first().map { it.name }
        )
    }

    @Test
    fun aisles_belongToTheirStore() = runBlocking {
        val cityMarket = storeDao.insertStore(StoreEntity(name = "City Market", color = "#E17055"))
        val cornerShop = storeDao.insertStore(StoreEntity(name = "Corner Shop", color = "#0984E3"))
        storeDao.replaceAisles(cityMarket, listOf(StoreAisleEntity(storeId = cityMarket, categoryId = produce, x = 0, y = 1)))
        storeDao.replaceAisles(cornerShop, listOf(StoreAisleEntity(storeId = cornerShop, categoryId = dairy, x = 3, y = 2)))

        val aisles = storeDao.observeAisles(cityMarket).first()
        assertEquals(1, aisles.size)
        assertEquals(produce, aisles.single().categoryId)
    }

    @Test
    fun replaceAisles_savesTheWholeNewMap() = runBlocking {
        val id = storeDao.insertStore(StoreEntity(name = "City Market", color = "#E17055"))
        storeDao.replaceAisles(id, listOf(
            StoreAisleEntity(storeId = id, categoryId = produce, x = 0, y = 1),
            StoreAisleEntity(storeId = id, categoryId = dairy, x = 2, y = 1)
        ))

        storeDao.replaceAisles(id, listOf(StoreAisleEntity(storeId = id, categoryId = dairy, x = 4, y = 5)))

        val aisle = storeDao.observeAisles(id).first().single()
        assertEquals(dairy, aisle.categoryId)
        assertEquals(4, aisle.x)
        assertEquals(5, aisle.y)
    }

    @Test
    fun entranceAndCheckout_areSavedWithTheStore() = runBlocking {
        val id = storeDao.insertStore(StoreEntity(name = "City Market", color = "#E17055"))

        storeDao.setLandmarks(id, entranceX = 2, entranceY = 0, checkoutX = 0, checkoutY = 8)

        val store = storeDao.observeStore(id).first()!!
        assertEquals(2 to 0, store.entranceX to store.entranceY)
        assertEquals(0 to 8, store.checkoutX to store.checkoutY)
    }

    @Test
    fun renamingAStore_keepsItsMap() = runBlocking {
        val id = storeDao.insertStore(StoreEntity(name = "City Market", color = "#E17055"))
        storeDao.replaceAisles(id, listOf(StoreAisleEntity(storeId = id, categoryId = produce, x = 0, y = 1)))

        storeDao.rename(id, "City Market North")

        assertEquals("City Market North", storeDao.observeStores().first().single().name)
        assertEquals(1, storeDao.observeAisles(id).first().size)
    }

    @Test
    fun deletingAStore_removesItsMap() = runBlocking {
        val id = storeDao.insertStore(StoreEntity(name = "City Market", color = "#E17055"))
        storeDao.replaceAisles(id, listOf(StoreAisleEntity(storeId = id, categoryId = produce, x = 0, y = 1)))

        storeDao.deleteStore(id)

        assertTrue(storeDao.observeStores().first().isEmpty())
        assertTrue(storeDao.observeAisles(id).first().isEmpty())
    }

    @Test
    fun aListRemembersItsStore() = runBlocking {
        val store = storeDao.insertStore(StoreEntity(name = "City Market", color = "#E17055"))
        val list = db.shoppingListDao().insert(ShoppingListEntity(name = "Weekly", storeId = store))

        assertEquals(store, db.shoppingListDao().getById(list)?.storeId)

        db.shoppingListDao().setStore(list, null)
        assertNull(db.shoppingListDao().getById(list)?.storeId)
    }

    @Test
    fun deletingAStore_unlinksItsListsButKeepsThem() = runBlocking {
        val store = storeDao.insertStore(StoreEntity(name = "City Market", color = "#E17055"))
        val list = db.shoppingListDao().insert(ShoppingListEntity(name = "Weekly", storeId = store))

        storeDao.deleteStore(store)

        val kept = db.shoppingListDao().getById(list)
        assertEquals("Weekly", kept?.name)
        assertNull(kept?.storeId)
    }

    @Test
    fun deletingACategory_removesItsAisleFromEveryMap() = runBlocking {
        val id = storeDao.insertStore(StoreEntity(name = "City Market", color = "#E17055"))
        storeDao.replaceAisles(id, listOf(
            StoreAisleEntity(storeId = id, categoryId = produce, x = 0, y = 1),
            StoreAisleEntity(storeId = id, categoryId = dairy, x = 2, y = 1)
        ))

        db.categoryDao().deleteCategoryById(dairy)

        assertEquals(listOf(produce), storeDao.observeAisles(id).first().map { it.categoryId })
    }

    @Test
    fun storeColors_areReadForPickingTheNextOne() = runBlocking {
        storeDao.insertStore(StoreEntity(name = "City Market", color = "#E17055"))
        storeDao.insertStore(StoreEntity(name = "Corner Shop", color = "#0984E3"))

        assertEquals(setOf("#E17055", "#0984E3"), storeDao.getStoreColors().toSet())
    }
}
