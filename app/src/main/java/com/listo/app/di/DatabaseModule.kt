package com.listo.app.di

import android.content.Context
import androidx.room.Room
import com.listo.app.data.database.DatabaseMigrations
import com.listo.app.data.database.LisToDatabase
import com.listo.app.data.database.dao.CategoryDao
import com.listo.app.data.database.dao.ShoppingItemDao
import com.listo.app.data.database.dao.ShoppingListDao
import com.listo.app.data.database.dao.StoreDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

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
            .addMigrations(*DatabaseMigrations.ALL)
            .fallbackToDestructiveMigrationFrom(1)
            .build()

    @Provides
    fun provideShoppingListDao(db: LisToDatabase): ShoppingListDao = db.shoppingListDao()

    @Provides
    fun provideShoppingItemDao(db: LisToDatabase): ShoppingItemDao = db.shoppingItemDao()

    @Provides
    fun provideCategoryDao(db: LisToDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideStoreDao(db: LisToDatabase): StoreDao = db.storeDao()
}
