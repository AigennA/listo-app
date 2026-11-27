package com.listo.app.ui.viewmodel

import com.listo.app.data.models.Store
import com.listo.app.data.repository.ShoppingRepository
import com.listo.app.data.repository.StoreRepository
import com.listo.app.domain.spending.BoughtItem
import com.listo.app.domain.spending.PeriodCalendar
import com.listo.app.domain.spending.SpendingPeriod
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class SpendingViewModelTest {

    private val utc = TimeZone.getTimeZone("UTC")
    private val calendar = PeriodCalendar(utc, Calendar.MONDAY)

    private fun day(year: Int, month: Int, d: Int): Long =
        Calendar.getInstance(utc).apply { clear(); set(year, month - 1, d, 12, 0, 0) }.timeInMillis

    private val now = day(2026, 11, 27)
    private val items = listOf(
        BoughtItem(100.0, "Produce", null, 1L, day(2026, 11, 2), name = "Tomato"),
        BoughtItem(60.0, "Dairy", null, 1L, day(2026, 10, 20), name = "Milk"),
        BoughtItem(40.0, "Dairy", null, null, day(2026, 11, 24), name = "Cheese")
    )

    private lateinit var viewModel: SpendingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val shopping = mockk<ShoppingRepository> { every { getBoughtItems() } returns flowOf(items) }
        val stores = mockk<StoreRepository> {
            every { observeStores() } returns flowOf(listOf(Store(id = 1, name = "Willys", color = "#E17055")))
        }
        viewModel = SpendingViewModel(shopping, stores, calendar) { now }
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val state get() = viewModel.uiState.value

    @Test
    fun `it opens on this month`() {
        assertEquals(SpendingPeriod.Month(2026, 11), state.period)
        assertEquals(140.0, state.summary.total, 0.001)
        assertEquals(60.0, state.summary.previousTotal, 0.001)
    }

    @Test
    fun `switching to week shows this week`() {
        viewModel.showWeeks()
        assertEquals(SpendingPeriod.weekOf(now, calendar), state.period)
        assertEquals(40.0, state.summary.total, 0.001)
    }

    @Test
    fun `stepping back shows the month before`() {
        viewModel.previous()
        assertEquals(SpendingPeriod.Month(2026, 10), state.period)
        assertEquals(60.0, state.summary.total, 0.001)
    }

    @Test
    fun `a custom range covers the chosen days`() {
        viewModel.showCustom(day(2026, 11, 1), day(2026, 11, 18))
        assertEquals(100.0, state.summary.total, 0.001)
    }

    @Test
    fun `items of the period are kept for the CSV file`() {
        assertEquals(listOf("Tomato", "Cheese"), state.itemsInPeriod.map { it.name })
    }

    @Test
    fun `store names come from My stores`() {
        assertEquals("Willys", state.storeNames[1L])
        assertTrue(state.canGoForward.not())
    }
}
