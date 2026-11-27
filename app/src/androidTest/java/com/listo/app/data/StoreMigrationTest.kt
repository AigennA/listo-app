package com.listo.app.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.listo.app.data.database.DatabaseMigrations
import com.listo.app.data.database.LisToDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Store maps arrive with schema v5 and v6. Updating the app must keep every
 * list, item and category a user already has.
 */
@RunWith(AndroidJUnit4::class)
class StoreMigrationTest {

    private val dbName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        LisToDatabase::class.java
    )

    private fun openLatest(): LisToDatabase = Room.databaseBuilder(
        InstrumentationRegistry.getInstrumentation().targetContext,
        LisToDatabase::class.java,
        dbName
    ).addMigrations(*DatabaseMigrations.ALL).build()

    @Test
    fun migratingFromTheReleasedSchema_keepsListsItemsAndCategories() {
        helper.createDatabase(dbName, 4).apply {
            execSQL("INSERT INTO categories (id, name, color) VALUES (1, 'Produce', '#2ECC71')")
            execSQL("INSERT INTO shopping_lists (id, name, createdAt, isArchived) VALUES (1, 'Weekly', 100, 0)")
            execSQL(
                "INSERT INTO shopping_items (id, listId, name, quantity, categoryId, isChecked, createdAt) " +
                    "VALUES (1, 1, 'Tomato', '2', 1, 0, 100)"
            )
            close()
        }

        helper.runMigrationsAndValidate(dbName, DatabaseMigrations.LATEST, true, *DatabaseMigrations.ALL)

        val db = openLatest()
        runBlocking {
            val list = db.shoppingListDao().getById(1)
            assertEquals("Weekly", list?.name)
            assertNull(list?.storeId)
            val item = db.shoppingItemDao().getItemsForList(1).first().single()
            assertEquals("Tomato", item.name)
            assertEquals(1L, item.categoryId)
            assertEquals(1, db.categoryDao().getAllCategories().first().size)
            assertTrue(db.storeDao().observeStores().first().isEmpty())
        }
        db.close()
    }

    @Test
    fun migrating6To7_keepsItemsAndTheirOldQuantityText() {
        helper.createDatabase(dbName, 6).apply {
            execSQL("INSERT INTO shopping_lists (id, name, createdAt, isArchived) VALUES (1, 'Weekly', 100, 0)")
            execSQL(
                "INSERT INTO shopping_items (id, listId, name, quantity, isChecked, createdAt, price) " +
                    "VALUES (1, 1, 'Rice', '2 kg', 0, 100, 30.0)"
            )
            close()
        }

        helper.runMigrationsAndValidate(dbName, 7, true, *DatabaseMigrations.ALL)

        val db = openLatest()
        runBlocking {
            val item = db.shoppingItemDao().getItemsForList(1).first().single()
            assertEquals("2 kg", item.quantity)
            assertNull(item.amount)
            assertNull(item.unit)
            assertEquals(30.0, db.shoppingListDao().getActiveLists().first().single().totalPrice, 0.001)
        }
        db.close()
    }

    @Test
    fun migrating7To8_givesExistingStoresTheDefaultEntranceAndCheckout() {
        helper.createDatabase(dbName, 7).apply {
            execSQL("INSERT INTO stores (id, name, color, createdAt) VALUES (1, 'Willys', '#E17055', 100)")
            close()
        }

        helper.runMigrationsAndValidate(dbName, 8, true, *DatabaseMigrations.ALL)

        val db = openLatest()
        runBlocking {
            val store = db.storeDao().observeStores().first().single()
            assertEquals("Willys", store.name)
            assertEquals(0 to 0, store.entranceX to store.entranceY)
            assertEquals(5 to 8, store.checkoutX to store.checkoutY)
        }
        db.close()
    }

    @Test
    fun migrating5To6_keepsStoresAndTheirMaps() {
        helper.createDatabase(dbName, 5).apply {
            execSQL("INSERT INTO categories (id, name, color) VALUES (1, 'Produce', '#2ECC71')")
            execSQL("INSERT INTO stores (id, name, color, createdAt) VALUES (1, 'Willys', '#E17055', 100)")
            execSQL("INSERT INTO store_aisles (id, storeId, categoryId, x, y) VALUES (1, 1, 1, 0, 1)")
            execSQL("INSERT INTO shopping_lists (id, name, createdAt, isArchived) VALUES (1, 'Weekly', 100, 0)")
            close()
        }

        helper.runMigrationsAndValidate(dbName, 6, true, *DatabaseMigrations.ALL)

        val db = openLatest()
        runBlocking {
            assertEquals("Willys", db.storeDao().observeStores().first().single().name)
            assertEquals(1, db.storeDao().observeAisles(1).first().size)
            assertEquals("Weekly", db.shoppingListDao().getById(1)?.name)
        }
        db.close()
    }
}
