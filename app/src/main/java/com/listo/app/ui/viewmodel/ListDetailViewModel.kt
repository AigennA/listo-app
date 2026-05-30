package com.listo.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.Category
import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.models.ShoppingList
import com.listo.app.domain.usecases.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListDetailUiState(
    val list: ShoppingList? = null,
    val items: List<ShoppingItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true
) {
    val itemCount: Int get() = items.size
    val checkedCount: Int get() = items.count { it.isChecked }
    val progress: Float get() = if (items.isEmpty()) 0f else checkedCount.toFloat() / items.size
    val total: Double get() = items.sumOf { it.price ?: 0.0 }

    /** Items grouped by category name (uncategorised under "Other"), checked items last. */
    val grouped: List<Pair<String, List<ShoppingItem>>>
        get() = items
            .groupBy { it.categoryName ?: "Other" }
            .toList()
            .sortedBy { it.first == "Other" } // keep "Other" at the end
}

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    getShoppingListsUseCase: GetShoppingListsUseCase,
    getShoppingItemsUseCase: GetShoppingItemsUseCase,
    getCategoriesUseCase: GetCategoriesUseCase,
    private val addItemUseCase: AddShoppingItemUseCase,
    private val updateItemUseCase: UpdateShoppingItemUseCase,
    private val deleteItemUseCase: DeleteShoppingItemUseCase,
    private val updateListUseCase: UpdateShoppingListUseCase,
    private val deleteListUseCase: DeleteShoppingListUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val listId: Long = savedStateHandle.get<Long>("listId") ?: -1L

    val uiState: StateFlow<ListDetailUiState> = combine(
        getShoppingListsUseCase.byId(listId),
        getShoppingItemsUseCase.forList(listId),
        getCategoriesUseCase()
    ) { list, items, categories ->
        ListDetailUiState(
            list = list,
            items = items,
            categories = categories,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListDetailUiState()
    )

    fun addItem(name: String, quantity: String, categoryId: Long?, price: Double?) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            addItemUseCase(
                ShoppingItem(
                    listId = listId,
                    name = trimmed,
                    quantity = quantity.trim(),
                    categoryId = categoryId,
                    price = price
                )
            )
        }
    }

    fun toggleItem(item: ShoppingItem) {
        viewModelScope.launch { updateItemUseCase.toggle(item.id, !item.isChecked) }
    }

    fun updateItem(item: ShoppingItem) {
        viewModelScope.launch { updateItemUseCase(item) }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch { deleteItemUseCase(id) }
    }

    fun renameList(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { updateListUseCase.rename(listId, name) }
    }

    fun setBudget(budget: Double?) {
        viewModelScope.launch { updateListUseCase.setBudget(listId, budget) }
    }

    fun setSchedule(scheduledDate: Long?, reminderAt: Long?) {
        viewModelScope.launch { updateListUseCase.setSchedule(listId, scheduledDate, reminderAt) }
    }

    fun archiveList(onDone: () -> Unit) {
        viewModelScope.launch {
            updateListUseCase.archive(listId)
            onDone()
        }
    }

    fun deleteList(onDone: () -> Unit) {
        viewModelScope.launch {
            deleteListUseCase(listId)
            onDone()
        }
    }
}
