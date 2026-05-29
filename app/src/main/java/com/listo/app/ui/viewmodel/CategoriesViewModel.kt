package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.Category
import com.listo.app.domain.usecases.CreateCategoryUseCase
import com.listo.app.domain.usecases.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val showCreateDialog: Boolean = false,
    val newCategoryName: String = "",
    val newCategoryColor: String = "#2ECC71"
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCategoriesUseCase().collect { cats ->
                _uiState.update { it.copy(categories = cats, isLoading = false) }
            }
        }
    }

    fun showDialog() = _uiState.update { it.copy(showCreateDialog = true) }
    fun hideDialog() = _uiState.update { it.copy(showCreateDialog = false, newCategoryName = "", newCategoryColor = "#2ECC71") }
    fun updateName(value: String) = _uiState.update { it.copy(newCategoryName = value) }
    fun updateColor(value: String) = _uiState.update { it.copy(newCategoryColor = value) }

    fun createCategory() {
        val state = _uiState.value
        if (state.newCategoryName.isBlank()) return
        viewModelScope.launch {
            createCategoryUseCase(
                Category(name = state.newCategoryName.trim(), color = state.newCategoryColor)
            )
            hideDialog()
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch { createCategoryUseCase.delete(id) }
    }
}
