package com.listo.app.ui.viewmodel

import com.listo.app.data.models.ShoppingList
import com.listo.app.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarUiStateTest {

    private val oneDayMs = 24L * 60 * 60 * 1000

    @Test
    fun `scheduledDays groups lists by start of day and ignores unscheduled`() {
        val day = DateUtils.startOfDay(System.currentTimeMillis())
        val state = CalendarUiState(
            lists = listOf(
                ShoppingList(id = 1, name = "morning", scheduledDate = day + 9 * 60 * 60 * 1000),
                ShoppingList(id = 2, name = "evening", scheduledDate = day + 20 * 60 * 60 * 1000),
                ShoppingList(id = 3, name = "unscheduled", scheduledDate = null)
            )
        )
        assertEquals(setOf(day), state.scheduledDays)
    }

    @Test
    fun `listsOn returns only lists scheduled that day`() {
        val day = DateUtils.startOfDay(System.currentTimeMillis())
        val state = CalendarUiState(
            lists = listOf(
                ShoppingList(id = 1, name = "today", scheduledDate = day + 60_000),
                ShoppingList(id = 2, name = "tomorrow", scheduledDate = day + oneDayMs + 60_000)
            )
        )
        assertEquals(1, state.listsOn(day).size)
        assertEquals("today", state.listsOn(day).single().name)
        assertEquals(1, state.listsOn(day + oneDayMs).size)
    }
}
