package com.listo.app.domain.usecases

import com.listo.app.data.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(private val repo: TaskRepository) {
    suspend operator fun invoke(id: Long) = repo.deleteTaskById(id)
}
