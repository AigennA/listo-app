package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.ShoppingList
import com.listo.app.domain.usecases.CreateShoppingListUseCase
import com.listo.app.domain.usecases.GetShoppingListsUseCase
import com.listo.app.domain.usecases.UpdateShoppingListUseCase
import com.listo.app.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalendarUiState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = true
) {
    /** Start-of-day millis that have at least one scheduled list. */
    val scheduledDays: Set<Long>
        get() = lists.mapNotNull { it.scheduledDate?.let(DateUtils::startOfDay) }.toSet()

    fun listsOn(dayStart: Long): List<ShoppingList> =
        lists.filter { it.scheduledDate != null && DateUtils.startOfDay(it.scheduledDate) == dayStart }
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    getShoppingListsUseCase: GetShoppingListsUseCase,
    private val createListUseCase: CreateShoppingListUseCase,
    private val updateListUseCase: UpdateShoppingListUseCase
) : ViewModel() {

    val uiState: StateFlow<CalendarUiState> = getShoppingListsUseCase.active()
        .map { CalendarUiState(lists = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarUiState()
        )

    /** Creates a list scheduled for [dayStart] with an optional reminder time. */
    fun createListForDay(
        name: String,
        dayStart: Long,
        reminderAt: Long?,
        onCreated: (listId: Long, reminderAt: Long?) -> Unit
    ) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val id = createListUseCase(trimmed, dayStart)
            if (reminderAt != null) {
                updateListUseCase.setSchedule(id, dayStart, reminderAt)
            }
            onCreated(id, reminderAt)
        }
    }
}
