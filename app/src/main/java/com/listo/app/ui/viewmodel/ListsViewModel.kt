package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.ShoppingList
import com.listo.app.domain.usecases.CreateShoppingListUseCase
import com.listo.app.domain.usecases.DeleteShoppingListUseCase
import com.listo.app.domain.usecases.GetShoppingListsUseCase
import com.listo.app.domain.usecases.SeedCategoriesUseCase
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
}
