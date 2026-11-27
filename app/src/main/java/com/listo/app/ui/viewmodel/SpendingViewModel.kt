package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.repository.ShoppingRepository
import com.listo.app.data.repository.StoreRepository
import com.listo.app.domain.spending.BoughtItem
import com.listo.app.domain.spending.PeriodCalendar
import com.listo.app.domain.spending.SpendingPeriod
import com.listo.app.domain.spending.SpendingSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** The current time; swapped for a fixed one in tests. */
fun interface Clock {
    fun now(): Long
}

data class SpendingUiState(
    val period: SpendingPeriod,
    val summary: SpendingSummary = SpendingSummary(0.0, 0.0, emptyList(), emptyList(), 0, 0),
    /** Bought items of [period], for the CSV file. */
    val itemsInPeriod: List<BoughtItem> = emptyList(),
    val storeNames: Map<Long, String> = emptyMap(),
    val storeColors: Map<Long, String> = emptyMap(),
    /** False once the next period would lie in the future. */
    val canGoForward: Boolean = false,
    val isLoading: Boolean = true
)

/** Spending by week, month or a chosen range, by category and store. */
@HiltViewModel
class SpendingViewModel @Inject constructor(
    shopping: ShoppingRepository,
    stores: StoreRepository,
    private val calendar: PeriodCalendar,
    private val clock: Clock
) : ViewModel() {

    private val period = MutableStateFlow<SpendingPeriod>(SpendingPeriod.monthOf(clock.now(), calendar))

    val uiState: StateFlow<SpendingUiState> = combine(
        period,
        shopping.getBoughtItems(),
        stores.observeStores()
    ) { current, items, storeList ->
        val range = current.range(calendar)
        SpendingUiState(
            period = current,
            summary = SpendingSummary.of(items, range, current.previous(calendar).range(calendar)),
            itemsInPeriod = items.filter { it.boughtAt in range }.sortedBy { it.boughtAt },
            storeNames = storeList.associate { it.id to it.name },
            storeColors = storeList.associate { it.id to it.color },
            canGoForward = current.next(calendar).range(calendar).start <= clock.now(),
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SpendingUiState(period.value))

    fun showMonths() { period.value = SpendingPeriod.monthOf(clock.now(), calendar) }

    fun showWeeks() { period.value = SpendingPeriod.weekOf(clock.now(), calendar) }

    /** From the day of [from] through the day of [to]. */
    fun showCustom(from: Long, to: Long) {
        period.value = SpendingPeriod.Custom(minOf(from, to), maxOf(from, to))
    }

    fun previous() { period.value = period.value.previous(calendar) }

    fun next() {
        if (uiState.value.canGoForward) period.value = period.value.next(calendar)
    }
}
