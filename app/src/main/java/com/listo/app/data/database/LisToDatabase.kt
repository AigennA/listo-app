package com.listo.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.listo.app.data.database.dao.CategoryDao
import com.listo.app.data.database.dao.TaskDao
import com.listo.app.data.database.entities.CategoryEntity
import com.listo.app.data.database.entities.TaskEntity

@Database(
    entities = [TaskEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LisToDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "listo_database"
    }
}
