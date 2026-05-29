package com.listo.app.domain.usecases

import com.listo.app.data.models.Task
import com.listo.app.data.repository.TaskRepository
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(private val repo: TaskRepository) {
    suspend operator fun invoke(task: Task): Long = repo.insertTask(task)
}
