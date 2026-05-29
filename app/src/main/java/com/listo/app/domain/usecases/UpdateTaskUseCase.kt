package com.listo.app.domain.usecases

import com.listo.app.data.models.Task
import com.listo.app.data.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(private val repo: TaskRepository) {
    suspend operator fun invoke(task: Task) = repo.updateTask(task)
    suspend fun toggleCompletion(id: Long, isCompleted: Boolean) =
        repo.updateTaskCompletion(id, isCompleted)
}
