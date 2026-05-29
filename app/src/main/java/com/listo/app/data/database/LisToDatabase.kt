package com.listo.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.listo.app.data.database.dao.CategoryDao
import com.listo.app.data.database.dao.ShoppingItemDao
import com.listo.app.data.database.dao.ShoppingListDao
import com.listo.app.data.database.entities.CategoryEntity
import com.listo.app.data.database.entities.ShoppingItemEntity
import com.listo.app.data.database.entities.ShoppingListEntity

@Database(
    entities = [ShoppingListEntity::class, ShoppingItemEntity::class, CategoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class LisToDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "listo_database"
    }
}
