package com.listo.app.domain.spending

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class SpendingExportTest {

    private val summary = SpendingSummary(
        total = 380.0,
        previousTotal = 80.0,
        byCategory = listOf(
            SpendingShare("Meat & Fish", 200.0, 0.526, null),
            SpendingShare("Produce", 150.0, 0.394, null),
            SpendingShare<String?>(null, 30.0, 0.079, null)
        ),
        byStore = listOf(SpendingShare<Long?>(1L, 380.0, 1.0, null)),
        boughtCount = 5,
        unpricedCount = 1
    )

    private val labels = SpendingTextLabels(
        title = "Spending",
        total = "Total",
        byCategory = "By category",
        byStore = "By store",
        unpriced = { n -> "$n without a price" },
        category = { it ?: "Other" },
        store = { if (it == 1L) "City Market" else "No store" },
        money = { "%.0f kr".format(Locale.US, it) }
    )

    @Test
    fun `the text summary is ready to paste`() {
        val text = SpendingExport.text(summary, "November 2026", labels)
        assertEquals(
            """
            Spending · November 2026
            Total: 380 kr

            By category
            Meat & Fish: 200 kr (53%)
            Produce: 150 kr (39%)
            Other: 30 kr (8%)

            By store
            City Market: 380 kr (100%)

            1 without a price
            """.trimIndent(),
            text
        )
    }

    @Test
    fun `a period with every item priced has no note`() {
        val text = SpendingExport.text(summary.copy(unpricedCount = 0), "November 2026", labels)
        assertTrue(!text.contains("without a price"))
    }

    private val items = listOf(
        BoughtItem(15.5, "Dairy", null, 1L, 1_764_547_200_000, name = "Milk"),   // 2025-12-01 UTC
        BoughtItem(null, null, null, null, 1_764_633_600_000, name = "Bread, white")
    )

    private val columns = SpendingCsvColumns("Date", "Item", "Category", "Store", "Amount")

    @Test
    fun `csv uses comma and dot where the decimal sign is a dot`() {
        val csv = SpendingExport.csv(items, columns, labels, Locale.US, TimeZone.getTimeZone("UTC"))
        assertEquals(
            "Date,Item,Category,Store,Amount\n" +
                "2025-12-01,Milk,Dairy,City Market,15.50\n" +
                "2025-12-02,\"Bread, white\",Other,No store,\n",
            csv
        )
    }

    @Test
    fun `csv uses semicolon and comma where the decimal sign is a comma`() {
        val csv = SpendingExport.csv(items.take(1), columns, labels, Locale.forLanguageTag("sv-SE"), TimeZone.getTimeZone("UTC"))
        assertEquals("Date;Item;Category;Store;Amount\n2025-12-01;Milk;Dairy;City Market;15,50\n", csv)
    }

    @Test
    fun `quotes inside a value are doubled`() {
        val quoted = listOf(BoughtItem(1.0, null, null, null, 0, name = "12\" pizza"))
        val csv = SpendingExport.csv(quoted, columns, labels, Locale.US, TimeZone.getTimeZone("UTC"))
        assertTrue(csv.contains("\"12\"\" pizza\""))
    }
}
