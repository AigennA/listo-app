package com.listo.app.data.repository

import com.listo.app.data.database.dao.CategoryDao
import com.listo.app.data.database.dao.TaskDao
import com.listo.app.data.database.entities.TaskEntity
import com.listo.app.data.database.entities.toDomain
import com.listo.app.data.database.entities.toEntity
import com.listo.app.data.models.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao
) {

    fun getAllTasks(): Flow<List<Task>> =
        taskDao.getAllTasks().map { entities ->
            entities.map { entity -> enrichWithCategory(entity) }
        }

    fun getTasksForToday(startOfDay: Long, endOfDay: Long): Flow<List<Task>> =
        taskDao.getTasksForToday(startOfDay, endOfDay).map { entities ->
            entities.map { entity -> enrichWithCategory(entity) }
        }

    fun getPendingTasks(): Flow<List<Task>> =
        taskDao.getPendingTasks().map { entities ->
            entities.map { entity -> enrichWithCategory(entity) }
        }

    fun getCompletedTasks(): Flow<List<Task>> =
        taskDao.getCompletedTasks().map { entities ->
            entities.map { entity -> enrichWithCategory(entity) }
        }

    fun getCompletedCount(): Flow<Int> = taskDao.getCompletedCount()

    fun getPendingCount(): Flow<Int> = taskDao.getPendingCount()

    fun getCompletedCountForWeek(startOfWeek: Long, endOfWeek: Long): Flow<Int> =
        taskDao.getCompletedCountForWeek(startOfWeek, endOfWeek)

    suspend fun getTaskById(id: Long): Task? =
        taskDao.getTaskById(id)?.let { enrichWithCategory(it) }

    fun observeTaskById(id: Long): Flow<Task?> =
        taskDao.observeTaskById(id).map { entity -> entity?.let { enrichWithCategory(it) } }

    suspend fun insertTask(task: Task): Long =
        taskDao.insertTask(task.toEntity())

    suspend fun updateTask(task: Task) =
        taskDao.updateTask(task.toEntity())

    suspend fun deleteTask(task: Task) =
        taskDao.deleteTask(task.toEntity())

    suspend fun deleteTaskById(id: Long) =
        taskDao.deleteTaskById(id)

    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean) =
        taskDao.updateTaskCompletion(id, isCompleted)

    private suspend fun enrichWithCategory(entity: TaskEntity): Task {
        val category = entity.categoryId?.let { categoryDao.getCategoryById(it) }
        return entity.toDomain(
            categoryName = category?.name,
            categoryColor = category?.color
        )
    }
}
