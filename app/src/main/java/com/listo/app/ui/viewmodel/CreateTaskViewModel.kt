package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.Category
import com.listo.app.data.models.Priority
import com.listo.app.data.models.Task
import com.listo.app.data.repository.TaskRepository
import com.listo.app.domain.usecases.CreateTaskUseCase
import com.listo.app.domain.usecases.GetCategoriesUseCase
import com.listo.app.domain.usecases.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTaskUiState(
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.LOW,
    val dueDate: Long? = null,
    val selectedCategoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.getTaskById(taskId)?.let { task ->
                _uiState.update {
                    it.copy(
                        title = task.title,
                        description = task.description,
                        priority = task.priority,
                        dueDate = task.dueDate,
                        selectedCategoryId = task.categoryId
                    )
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun updateTitle(value: String) = _uiState.update { it.copy(title = value) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun updatePriority(value: Priority) = _uiState.update { it.copy(priority = value) }
    fun updateDueDate(value: Long?) = _uiState.update { it.copy(dueDate = value) }
    fun updateCategory(id: Long?) = _uiState.update { it.copy(selectedCategoryId = id) }

    fun saveTask(existingTaskId: Long? = null) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title cannot be empty") }
            return
        }
        viewModelScope.launch {
            val task = Task(
                id = existingTaskId ?: 0L,
                title = state.title.trim(),
                description = state.description.trim(),
                priority = state.priority,
                dueDate = state.dueDate,
                categoryId = state.selectedCategoryId
            )
            if (existingTaskId != null) {
                updateTaskUseCase(task)
            } else {
                createTaskUseCase(task)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
