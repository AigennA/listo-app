package com.listo.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.Task
import com.listo.app.domain.usecases.DeleteTaskUseCase
import com.listo.app.domain.usecases.GetTasksUseCase
import com.listo.app.domain.usecases.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailUiState(
    val task: Task? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    getTasksUseCase: GetTasksUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: -1L

    val uiState: StateFlow<TaskDetailUiState> = getTasksUseCase.byId(taskId)
        .map { task -> TaskDetailUiState(task = task, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaskDetailUiState()
        )

    fun toggleCompletion() {
        val current = uiState.value.task ?: return
        viewModelScope.launch {
            updateTaskUseCase.toggleCompletion(current.id, !current.isCompleted)
        }
    }

    fun deleteTask(onDeleted: () -> Unit) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
            onDeleted()
        }
    }
}
