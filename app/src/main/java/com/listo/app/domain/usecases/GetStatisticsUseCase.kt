package com.listo.app.domain.usecases

import com.listo.app.data.repository.TaskRepository
import com.listo.app.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStatisticsUseCase @Inject constructor(private val repo: TaskRepository) {
    fun completedCount(): Flow<Int> = repo.getCompletedCount()
    fun pendingCount(): Flow<Int> = repo.getPendingCount()
    fun completedThisWeek(): Flow<Int> {
        val (start, end) = DateUtils.currentWeekRange()
        return repo.getCompletedCountForWeek(start, end)
    }
}
