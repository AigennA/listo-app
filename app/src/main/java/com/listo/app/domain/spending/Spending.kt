package com.listo.app.domain.spending

import java.util.Calendar
import java.util.TimeZone

/** From [start] up to, not including, [endExclusive], in epoch millis. */
data class TimeRange(val start: Long, val endExclusive: Long) {
    operator fun contains(time: Long): Boolean = time >= start && time < endExclusive
}

/** The time zone and week start used to cut time into weeks and months. */
class PeriodCalendar(
    private val timeZone: TimeZone = TimeZone.getDefault(),
    private val firstDayOfWeek: Int = Calendar.getInstance().firstDayOfWeek
) {
    fun at(time: Long): Calendar = Calendar.getInstance(timeZone).apply {
        this.firstDayOfWeek = this@PeriodCalendar.firstDayOfWeek
        timeInMillis = time
    }

    fun startOfDay(time: Long): Long = at(time).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** [time] moved by whole calendar days, so daylight-saving changes don't shift it. */
    fun addDays(time: Long, days: Int): Long = at(time).apply { add(Calendar.DAY_OF_MONTH, days) }.timeInMillis

    fun startOfWeek(time: Long): Long {
        val day = at(startOfDay(time))
        val back = (day.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek + 7) % 7
        return addDays(day.timeInMillis, -back)
    }

    fun startOfMonth(year: Int, month: Int): Long = Calendar.getInstance(timeZone).apply {
        clear()
        set(year, month - 1, 1)
    }.timeInMillis
}

/** A span of time to add spending up over: a week, a month, or days the user picks. */
sealed interface SpendingPeriod {
    fun range(calendar: PeriodCalendar): TimeRange
    fun previous(calendar: PeriodCalendar): SpendingPeriod
    fun next(calendar: PeriodCalendar): SpendingPeriod

    /** [month] is 1-based. */
    data class Month(val year: Int, val month: Int) : SpendingPeriod {
        override fun range(calendar: PeriodCalendar): TimeRange {
            val start = calendar.startOfMonth(year, month)
            val end = calendar.at(start).apply { add(Calendar.MONTH, 1) }.timeInMillis
            return TimeRange(start, end)
        }

        override fun previous(calendar: PeriodCalendar) = shifted(calendar, -1)
        override fun next(calendar: PeriodCalendar) = shifted(calendar, 1)

        private fun shifted(calendar: PeriodCalendar, months: Int): Month {
            val moved = calendar.at(calendar.startOfMonth(year, month)).apply { add(Calendar.MONTH, months) }
            return Month(moved.get(Calendar.YEAR), moved.get(Calendar.MONTH) + 1)
        }
    }

    /** The week starting at midnight [start]. */
    data class Week(val start: Long) : SpendingPeriod {
        override fun range(calendar: PeriodCalendar) = TimeRange(start, calendar.addDays(start, DAYS_IN_WEEK))
        override fun previous(calendar: PeriodCalendar) = Week(calendar.addDays(start, -DAYS_IN_WEEK))
        override fun next(calendar: PeriodCalendar) = Week(calendar.addDays(start, DAYS_IN_WEEK))
    }

    /** From the day of [from] through the day of [to], both included. */
    data class Custom(val from: Long, val to: Long) : SpendingPeriod {
        override fun range(calendar: PeriodCalendar) =
            TimeRange(calendar.startOfDay(from), calendar.addDays(calendar.startOfDay(to), 1))

        fun days(calendar: PeriodCalendar): Int {
            var count = 0
            var day = calendar.startOfDay(from)
            val end = range(calendar).endExclusive
            while (day < end) { count++; day = calendar.addDays(day, 1) }
            return count
        }

        override fun previous(calendar: PeriodCalendar) = shifted(calendar, -days(calendar))
        override fun next(calendar: PeriodCalendar) = shifted(calendar, days(calendar))

        private fun shifted(calendar: PeriodCalendar, days: Int) =
            Custom(calendar.addDays(calendar.startOfDay(from), days), calendar.addDays(calendar.startOfDay(to), days))
    }

    companion object {
        private const val DAYS_IN_WEEK = 7

        fun monthOf(time: Long, calendar: PeriodCalendar): Month {
            val at = calendar.at(time)
            return Month(at.get(Calendar.YEAR), at.get(Calendar.MONTH) + 1)
        }

        fun weekOf(time: Long, calendar: PeriodCalendar): Week = Week(calendar.startOfWeek(time))
    }
}

/**
 * One bought item for spending: what it cost ([lineTotal], null when no price
 * was entered), its aisle and store, and when it was bought.
 */
data class BoughtItem(
    val lineTotal: Double?,
    val categoryName: String?,
    val categoryColor: String?,
    val storeId: Long?,
    val boughtAt: Long,
    val name: String = ""
)

/** How much of a period's spending went to one category or store. */
data class SpendingShare<K>(val key: K, val amount: Double, val share: Double, val color: String?)

/** Spending over a period, split by category and store, with the period before to compare. */
data class SpendingSummary(
    val total: Double,
    val previousTotal: Double,
    val byCategory: List<SpendingShare<String?>>,
    val byStore: List<SpendingShare<Long?>>,
    val boughtCount: Int,
    val unpricedCount: Int
) {
    /** More (positive) or less (negative) than the period before. */
    val change: Double get() = total - previousTotal

    companion object {
        fun of(items: List<BoughtItem>, range: TimeRange, previous: TimeRange): SpendingSummary {
            val inPeriod = items.filter { it.boughtAt in range }
            val priced = inPeriod.filter { it.lineTotal != null }
            val total = priced.sumOf { it.lineTotal!! }
            return SpendingSummary(
                total = total,
                previousTotal = items.filter { it.boughtAt in previous }.sumOf { it.lineTotal ?: 0.0 },
                byCategory = shares(priced, total, { it.categoryName }, { it.categoryColor }),
                byStore = shares(priced, total, { it.storeId }, { null }),
                boughtCount = inPeriod.size,
                unpricedCount = inPeriod.size - priced.size
            )
        }

        private fun <K> shares(
            priced: List<BoughtItem>,
            total: Double,
            key: (BoughtItem) -> K,
            color: (BoughtItem) -> String?
        ): List<SpendingShare<K>> = priced
            .groupBy(key)
            .map { (k, group) ->
                val amount = group.sumOf { it.lineTotal!! }
                SpendingShare(k, amount, if (total > 0) amount / total else 0.0, color(group.first()))
            }
            .sortedByDescending { it.amount }
    }
}
