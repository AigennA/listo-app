package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.Category
import com.listo.app.data.models.Priority
import com.listo.app.data.models.Task
import com.listo.app.domain.usecases.DeleteTaskUseCase
import com.listo.app.domain.usecases.GetCategoriesUseCase
import com.listo.app.domain.usecases.GetTasksUseCase
import com.listo.app.domain.usecases.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StatusFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Done")
}

data class HomeUiState(
    val todayTasks: List<Task> = emptyList(),
    val allTasks: List<Task> = emptyList(),
    val categories: List<Category> = emptyList(),
    val query: String = "",
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val priorityFilter: Priority? = null,
    val categoryFilter: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val completedToday get() = todayTasks.count { it.isCompleted }
    val totalToday get() = todayTasks.size
    val progressToday get() = if (totalToday == 0) 0f else completedToday.toFloat() / totalToday

    val isFiltering: Boolean
        get() = query.isNotBlank() ||
            statusFilter != StatusFilter.ALL ||
            priorityFilter != null ||
            categoryFilter != null

    /** Tasks matching the active search query and filters (over the full list). */
    val filteredTasks: List<Task>
        get() = allTasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true)
            val matchesStatus = when (statusFilter) {
                StatusFilter.ALL       -> true
                StatusFilter.ACTIVE    -> !task.isCompleted
                StatusFilter.COMPLETED -> task.isCompleted
            }
            val matchesPriority = priorityFilter == null || task.priority == priorityFilter
            val matchesCategory = categoryFilter == null || task.categoryId == categoryFilter
            matchesQuery && matchesStatus && matchesPriority && matchesCategory
        }
}

private data class Filters(
    val query: String,
    val status: StatusFilter,
    val priority: Priority?,
    val category: Long?
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _status = MutableStateFlow(StatusFilter.ALL)
    private val _priority = MutableStateFlow<Priority?>(null)
    private val _category = MutableStateFlow<Long?>(null)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            val filters = combine(_query, _status, _priority, _category) { q, s, p, c ->
                Filters(q, s, p, c)
            }
            combine(
                getTasksUseCase.today(),
                getTasksUseCase.all(),
                getCategoriesUseCase(),
                filters
            ) { todayTasks, allTasks, categories, f ->
                HomeUiState(
                    todayTasks = todayTasks,
                    allTasks = allTasks,
                    categories = categories,
                    query = f.query,
                    statusFilter = f.status,
                    priorityFilter = f.priority,
                    categoryFilter = f.category,
                    isLoading = false
                )
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onQueryChange(value: String) { _query.value = value }
    fun onStatusChange(value: StatusFilter) { _status.value = value }
    fun onPriorityChange(value: Priority?) { _priority.value = value }
    fun onCategoryChange(value: Long?) { _category.value = value }

    fun clearFilters() {
        _query.value = ""
        _status.value = StatusFilter.ALL
        _priority.value = null
        _category.value = null
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
