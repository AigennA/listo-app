package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.Task
import com.listo.app.domain.usecases.DeleteTaskUseCase
import com.listo.app.domain.usecases.GetTasksUseCase
import com.listo.app.domain.usecases.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val todayTasks: List<Task> = emptyList(),
    val allTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val completedToday get() = todayTasks.count { it.isCompleted }
    val totalToday get() = todayTasks.size
    val progressToday get() = if (totalToday == 0) 0f else completedToday.toFloat() / totalToday
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            combine(
                getTasksUseCase.today(),
                getTasksUseCase.all()
            ) { todayTasks, allTasks ->
                HomeUiState(
                    todayTasks = todayTasks,
                    allTasks = allTasks,
                    isLoading = false
                )
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            updateTaskUseCase.toggleCompletion(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
        }
    }
}
