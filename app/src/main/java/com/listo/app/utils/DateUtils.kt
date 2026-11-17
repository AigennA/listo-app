package com.listo.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private const val DATE_PATTERN = "MMM dd, yyyy"
    private const val TIME_12H_PATTERN = "hh:mm a"
    private const val TIME_PATTERN = "HH:mm"
    private const val SHORT_DATE_PATTERN = "MMM dd"

    /**
     * A formatter in the locale in force right now. Not cached: the in-app
     * language can change at runtime, and a cached formatter would keep the
     * language of first use (e.g. "Nov 19" in a Swedish UI).
     */
    private fun format(pattern: String, millis: Long): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))

    fun formatTime(millis: Long): String = format(TIME_PATTERN, millis)

    fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return start to cal.timeInMillis
    }

    fun currentWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        return start to cal.timeInMillis
    }

    fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun formatDate(millis: Long): String = format(DATE_PATTERN, millis)

    fun formatShortDate(millis: Long): String = format(SHORT_DATE_PATTERN, millis)

    fun formatDateTime(millis: Long): String =
        "${format(DATE_PATTERN, millis)} ${format(TIME_12H_PATTERN, millis)}"

    fun isOverdue(dueDate: Long?): Boolean =
        dueDate != null && dueDate < System.currentTimeMillis()

    fun isToday(millis: Long?): Boolean {
        if (millis == null) return false
        val (start, end) = todayRange()
        return millis in start until end
    }

    fun getDayLabel(dayOffset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -dayOffset)
        return SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
    }

    fun getDayRangeForOffset(dayOffset: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -dayOffset)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return start to cal.timeInMillis
    }
}
