package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.models.ShoppingList
import com.listo.app.domain.usecases.CreateShoppingListUseCase
import com.listo.app.domain.usecases.DeleteShoppingListUseCase
import com.listo.app.domain.usecases.DuplicateShoppingListUseCase
import com.listo.app.domain.usecases.GetShoppingItemsUseCase
import com.listo.app.domain.usecases.GetShoppingListsUseCase
import com.listo.app.domain.usecases.SeedCategoriesUseCase
import com.listo.app.domain.usecases.UpdateShoppingListUseCase
import com.listo.app.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListsUiState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = true,
    val showCreateDialog: Boolean = false,
    val newListName: String = ""
)

@HiltViewModel
class ListsViewModel @Inject constructor(
    getShoppingListsUseCase: GetShoppingListsUseCase,
    private val createListUseCase: CreateShoppingListUseCase,
    private val deleteListUseCase: DeleteShoppingListUseCase,
    private val updateListUseCase: UpdateShoppingListUseCase,
    private val duplicateListUseCase: DuplicateShoppingListUseCase,
    private val getShoppingItemsUseCase: GetShoppingItemsUseCase,
    private val seedCategoriesUseCase: SeedCategoriesUseCase,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _showDialog = MutableStateFlow(false)
    private val _newName = MutableStateFlow("")

    init {
        viewModelScope.launch {
            if (!prefs.isCategoriesSeeded.first()) {
                seedCategoriesUseCase()
                prefs.setCategoriesSeeded(true)
            }
        }
    }

    val uiState: StateFlow<ListsUiState> = combine(
        getShoppingListsUseCase.active(),
        _showDialog,
        _newName
    ) { lists, showDialog, name ->
        ListsUiState(
            lists = lists,
            isLoading = false,
            showCreateDialog = showDialog,
            newListName = name
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListsUiState()
    )

    fun showCreateDialog() { _showDialog.value = true }
    fun hideCreateDialog() { _showDialog.value = false; _newName.value = "" }
    fun updateNewName(value: String) { _newName.value = value }

    fun createList(onCreated: (Long) -> Unit) {
        val name = _newName.value.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = createListUseCase(name)
            hideCreateDialog()
            onCreated(id)
        }
    }

    fun deleteList(id: Long) {
        viewModelScope.launch { deleteListUseCase(id) }
    }

    fun renameList(id: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { updateListUseCase.rename(id, name) }
    }

    /** [copyName] is already localised by the screen, e.g. "Weekly (copy)". */
    fun duplicateList(list: ShoppingList, copyName: String) {
        viewModelScope.launch { duplicateListUseCase(list, copyName) }
    }

    /** Loads [list]'s items, then hands them to [onReady] (to build the share text). */
    fun withItems(list: ShoppingList, onReady: (List<ShoppingItem>) -> Unit) {
        viewModelScope.launch { onReady(getShoppingItemsUseCase.forList(list.id).first()) }
    }
}
