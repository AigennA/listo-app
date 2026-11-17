package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.ShoppingList
import com.listo.app.domain.usecases.DeleteShoppingListUseCase
import com.listo.app.domain.usecases.GetShoppingListsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getShoppingListsUseCase: GetShoppingListsUseCase,
    private val deleteListUseCase: DeleteShoppingListUseCase
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = getShoppingListsUseCase.archived()
        .map { HistoryUiState(lists = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState()
        )

    fun deleteList(id: Long) {
        viewModelScope.launch { deleteListUseCase(id) }
    }
}
