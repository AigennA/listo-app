package com.listo.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.listo.app.data.database.dao.CategoryDao
import com.listo.app.data.database.dao.ShoppingItemDao
import com.listo.app.data.database.dao.ShoppingListDao
import com.listo.app.data.database.dao.StoreDao
import com.listo.app.data.database.entities.CategoryEntity
import com.listo.app.data.database.entities.ShoppingItemEntity
import com.listo.app.data.database.entities.ShoppingListEntity
import com.listo.app.data.database.entities.StoreAisleEntity
import com.listo.app.data.database.entities.StoreEntity

@Database(
    entities = [
        ShoppingListEntity::class,
        ShoppingItemEntity::class,
        CategoryEntity::class,
        StoreEntity::class,
        StoreAisleEntity::class
    ],
    version = DatabaseMigrations.LATEST,
    exportSchema = true
)
abstract class LisToDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun storeDao(): StoreDao

    companion object {
        const val DATABASE_NAME = "listo_database"
    }
}
