package com.listo.app.data.repository

import com.listo.app.data.database.dao.CategoryDao
import com.listo.app.data.database.dao.TaskDao
import com.listo.app.data.database.entities.toDomain
import com.listo.app.data.database.entities.toEntity
import com.listo.app.data.models.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val taskDao: TaskDao
) {

    fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)?.toDomain()

    suspend fun insertCategory(category: Category): Long =
        categoryDao.insertCategory(category.toEntity())

    suspend fun updateCategory(category: Category) =
        categoryDao.updateCategory(category.toEntity())

    suspend fun deleteCategory(category: Category) =
        categoryDao.deleteCategory(category.toEntity())

    suspend fun deleteCategoryById(id: Long) =
        categoryDao.deleteCategoryById(id)
}
