package com.listo.app.domain.spending

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/** Words and formatting for the text summary, already in the app's language. */
data class SpendingTextLabels(
    val title: String,
    val total: String,
    val byCategory: String,
    val byStore: String,
    val unpriced: (Int) -> String,
    val category: (String?) -> String,
    val store: (Long?) -> String,
    val money: (Double) -> String
)

/** Column headings of the CSV file, in the app's language. */
data class SpendingCsvColumns(
    val date: String,
    val item: String,
    val category: String,
    val store: String,
    val amount: String
)

/** Turns spending into text to paste anywhere, or a CSV file for spreadsheets. */
object SpendingExport {

    fun text(summary: SpendingSummary, period: String, labels: SpendingTextLabels): String = buildString {
        appendLine("${labels.title} · $period")
        appendLine("${labels.total}: ${labels.money(summary.total)}")
        section(labels.byCategory, summary.byCategory, labels.category, labels.money)
        section(labels.byStore, summary.byStore, labels.store, labels.money)
        if (summary.unpricedCount > 0) {
            appendLine()
            appendLine(labels.unpriced(summary.unpricedCount))
        }
    }.trimEnd()

    private fun <K> StringBuilder.section(
        heading: String,
        shares: List<SpendingShare<K>>,
        name: (K) -> String,
        money: (Double) -> String
    ) {
        if (shares.isEmpty()) return
        appendLine()
        appendLine(heading)
        shares.forEach { appendLine("${name(it.key)}: ${money(it.amount)} (${(it.share * 100).roundToInt()}%)") }
    }

    /**
     * One row per bought item. Where the locale writes decimals with a comma,
     * values are separated by semicolons, as spreadsheets there expect.
     */
    fun csv(
        items: List<BoughtItem>,
        columns: SpendingCsvColumns,
        labels: SpendingTextLabels,
        locale: Locale,
        timeZone: TimeZone
    ): String {
        val symbols = DecimalFormatSymbols.getInstance(locale)
        val separator = if (symbols.decimalSeparator == ',') ';' else ','
        val amount = DecimalFormat("0.00", symbols)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { this.timeZone = timeZone }
        fun cell(value: String): String =
            if (value.any { it == separator || it == '"' || it == '\n' }) "\"" + value.replace("\"", "\"\"") + "\"" else value

        return buildString {
            append(listOf(columns.date, columns.item, columns.category, columns.store, columns.amount).joinToString(separator.toString()) { cell(it) })
            append('\n')
            items.sortedBy { it.boughtAt }.forEach { item ->
                val row = listOf(
                    date.format(Date(item.boughtAt)),
                    item.name,
                    labels.category(item.categoryName),
                    labels.store(item.storeId),
                    item.lineTotal?.let { amount.format(it) } ?: ""
                )
                append(row.joinToString(separator.toString()) { cell(it) })
                append('\n')
            }
        }
    }
}
