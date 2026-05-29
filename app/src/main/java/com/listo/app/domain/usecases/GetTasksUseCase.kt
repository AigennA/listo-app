package com.listo.app.domain.usecases

import com.listo.app.data.models.Task
import com.listo.app.data.repository.TaskRepository
import com.listo.app.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(private val repo: TaskRepository) {
    fun all(): Flow<List<Task>> = repo.getAllTasks()
    fun today(): Flow<List<Task>> {
        val (start, end) = DateUtils.todayRange()
        return repo.getTasksForToday(start, end)
    }
    fun pending(): Flow<List<Task>> = repo.getPendingTasks()
    fun completed(): Flow<List<Task>> = repo.getCompletedTasks()
}
