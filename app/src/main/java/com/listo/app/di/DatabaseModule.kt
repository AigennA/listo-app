package com.listo.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.listo.app.data.database.LisToDatabase
import com.listo.app.data.database.dao.CategoryDao
import com.listo.app.data.database.dao.ShoppingItemDao
import com.listo.app.data.database.dao.ShoppingListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** v2 -> v3 added the optional per-list budget column. Preserves user data. */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shopping_lists ADD COLUMN budget REAL")
        }
    }

    /** v3 -> v4 added a scheduled date and a reminder timestamp per list. */
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shopping_lists ADD COLUMN scheduledDate INTEGER")
            db.execSQL("ALTER TABLE shopping_lists ADD COLUMN reminderAt INTEGER")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LisToDatabase =
        Room.databaseBuilder(
            context,
            LisToDatabase::class.java,
            LisToDatabase.DATABASE_NAME
        )
            // Real migrations keep data across schema changes. Destructive
            // fallback only applies to the pre-shopping (v1 task) schema, which
            // has no migration path and predates any real data.
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideShoppingListDao(db: LisToDatabase): ShoppingListDao = db.shoppingListDao()

    @Provides
    fun provideShoppingItemDao(db: LisToDatabase): ShoppingItemDao = db.shoppingItemDao()

    @Provides
    fun provideCategoryDao(db: LisToDatabase): CategoryDao = db.categoryDao()
}
