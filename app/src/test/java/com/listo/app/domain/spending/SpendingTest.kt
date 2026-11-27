package com.listo.app.domain.spending

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/** Midnight UTC of a calendar day (month is 1-based). */
private fun day(year: Int, month: Int, dayOfMonth: Int, hour: Int = 0): Long =
    Calendar.getInstance(UTC).apply {
        clear()
        set(year, month - 1, dayOfMonth, hour, 0, 0)
    }.timeInMillis

class SpendingPeriodTest {

    private val calendar = PeriodCalendar(UTC, firstDayOfWeek = Calendar.MONDAY)

    @Test
    fun `a month runs from its first day up to the next month`() {
        val range = SpendingPeriod.Month(2026, 11).range(calendar)
        assertEquals(day(2026, 11, 1), range.start)
        assertEquals(day(2026, 12, 1), range.endExclusive)
    }

    @Test
    fun `the month of a moment is found`() {
        assertEquals(SpendingPeriod.Month(2026, 11), SpendingPeriod.monthOf(day(2026, 11, 27, 15), calendar))
    }

    @Test
    fun `a week starts on the locale's first day`() {
        // Friday 27 Nov 2026 lies in the week starting Monday 23 Nov.
        val week = SpendingPeriod.weekOf(day(2026, 11, 27, 15), calendar)
        val range = week.range(calendar)
        assertEquals(day(2026, 11, 23), range.start)
        assertEquals(day(2026, 11, 30), range.endExclusive)
    }

    @Test
    fun `a custom period includes both of its days`() {
        val custom = SpendingPeriod.Custom(day(2026, 11, 10), day(2026, 11, 27))
        val range = custom.range(calendar)
        assertEquals(day(2026, 11, 10), range.start)
        assertEquals(day(2026, 11, 28), range.endExclusive)
        assertEquals(18, custom.days(calendar))
    }

    @Test
    fun `stepping back and forth moves by a whole period`() {
        assertEquals(SpendingPeriod.Month(2026, 10), SpendingPeriod.Month(2026, 11).previous(calendar))
        assertEquals(SpendingPeriod.Month(2027, 1), SpendingPeriod.Month(2026, 12).next(calendar))
        val week = SpendingPeriod.weekOf(day(2026, 11, 27), calendar)
        assertEquals(day(2026, 11, 16), week.previous(calendar).range(calendar).start)
        assertEquals(day(2026, 11, 30), week.next(calendar).range(calendar).start)
    }

    @Test
    fun `the period before a custom one has the same length`() {
        val custom = SpendingPeriod.Custom(day(2026, 11, 10), day(2026, 11, 27))
        val before = custom.previous(calendar).range(calendar)
        assertEquals(day(2026, 10, 23), before.start)
        assertEquals(day(2026, 11, 10), before.endExclusive)
    }
}

class SpendingSummaryTest {

    private val november = TimeRange(day(2026, 11, 1), day(2026, 12, 1))
    private val october = TimeRange(day(2026, 10, 1), day(2026, 11, 1))

    private fun bought(total: Double?, category: String?, store: Long?, at: Long) =
        BoughtItem(lineTotal = total, categoryName = category, categoryColor = "#000000", storeId = store, boughtAt = at)

    private val items = listOf(
        bought(100.0, "Produce", 1, day(2026, 11, 2)),
        bought(50.0, "Produce", 2, day(2026, 11, 3)),
        bought(200.0, "Meat & Fish", 1, day(2026, 11, 20)),
        bought(30.0, null, null, day(2026, 11, 21)),
        bought(null, "Dairy", 1, day(2026, 11, 22)),
        bought(999.0, "Produce", 1, day(2026, 12, 1)),
        bought(80.0, "Produce", 1, day(2026, 10, 15))
    )

    private val summary = SpendingSummary.of(items, november, october)

    @Test
    fun `the total adds up priced items bought in the period`() {
        assertEquals(380.0, summary.total, 0.001)
    }

    @Test
    fun `categories are listed biggest first`() {
        assertEquals(listOf("Meat & Fish", "Produce", null), summary.byCategory.map { it.key })
        assertEquals(150.0, summary.byCategory[1].amount, 0.001)
    }

    @Test
    fun `stores are listed biggest first, items without a store together`() {
        assertEquals(listOf(1L, 2L, null), summary.byStore.map { it.key })
        assertEquals(300.0, summary.byStore.first().amount, 0.001)
    }

    @Test
    fun `items without a price are counted, not added`() {
        assertEquals(1, summary.unpricedCount)
        assertEquals(5, summary.boughtCount)
    }

    @Test
    fun `the previous period is there to compare`() {
        assertEquals(80.0, summary.previousTotal, 0.001)
        assertEquals(300.0, summary.change, 0.001)
    }

    @Test
    fun `shares add up to the whole`() {
        assertEquals(1.0, summary.byCategory.sumOf { it.share }, 0.001)
    }

    @Test
    fun `an empty period has nothing to show`() {
        val empty = SpendingSummary.of(emptyList(), november, october)
        assertEquals(0.0, empty.total, 0.0)
        assertEquals(emptyList<Any>(), empty.byCategory)
        assertNull(empty.byCategory.firstOrNull())
    }
}
