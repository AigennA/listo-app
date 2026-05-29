package com.listo.app.di

import android.content.Context
import androidx.room.Room
import com.listo.app.data.database.LisToDatabase
import com.listo.app.data.database.dao.CategoryDao
import com.listo.app.data.database.dao.TaskDao
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
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTaskDao(db: LisToDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideCategoryDao(db: LisToDatabase): CategoryDao = db.categoryDao()
}
