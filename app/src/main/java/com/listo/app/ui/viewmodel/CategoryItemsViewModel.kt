package com.listo.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.Category
import com.listo.app.data.models.ShoppingItem
import com.listo.app.domain.usecases.AddShoppingItemUseCase
import com.listo.app.domain.usecases.CreateShoppingListUseCase
import com.listo.app.domain.usecases.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryItemsViewModel @Inject constructor(
    getCategoriesUseCase: GetCategoriesUseCase,
    private val createListUseCase: CreateShoppingListUseCase,
    private val addItemUseCase: AddShoppingItemUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: -1L

    val category: StateFlow<Category?> = getCategoriesUseCase()
        .map { list -> list.firstOrNull { it.id == categoryId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    /** Creates a new list named after the category and adds the selected items. */
    fun createList(listName: String, selections: Map<String, Int>, onCreated: (Long) -> Unit) {
        val chosen = selections.filterValues { it > 0 }
        if (chosen.isEmpty()) return
        viewModelScope.launch {
            val listId = createListUseCase(listName)
            chosen.forEach { (name, qty) ->
                addItemUseCase(
                    ShoppingItem(
                        listId = listId,
                        name = name,
                        quantity = if (qty > 1) qty.toString() else "",
                        categoryId = categoryId
                    )
                )
            }
            onCreated(listId)
        }
    }
}
