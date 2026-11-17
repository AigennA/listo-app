package com.listo.app.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.repository.CategoryRepository
import com.listo.app.data.repository.ShoppingRepository
import com.listo.app.domain.usecases.SeedCategoriesUseCase
import com.listo.app.utils.GroceryCatalog
import com.listo.app.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Debug builds only: fills an empty install with the demo lists used for the
 * store screenshots and README.
 *
 *   adb shell am broadcast -a com.listo.app.debug.SEED_DEMO -p com.mopilappar.listo.debug
 *
 * Does nothing if any active list already exists, so it never mixes with real data.
 */
@AndroidEntryPoint
class DemoDataReceiver : BroadcastReceiver() {

    @Inject lateinit var shopping: ShoppingRepository
    @Inject lateinit var categories: CategoryRepository
    @Inject lateinit var seedCategories: SeedCategoriesUseCase
    @Inject lateinit var prefs: PreferencesManager

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                seed()
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun seed() {
        if (shopping.getActiveLists().first().isNotEmpty()) {
            Log.w(TAG, "Lists already exist; not seeding demo data")
            return
        }
        if (!prefs.isCategoriesSeeded.first()) {
            seedCategories()
            prefs.setCategoriesSeeded(true)
        }
        val categoryIds = categories.getAllCategories().first().associate { it.name to it.id }

        DemoData.lists.forEach { demo ->
            val listId = shopping.createList(demo.name)
            demo.budget?.let { shopping.setBudget(listId, it) }
            demo.daysAhead?.let { days ->
                val date = dayAt(days, DemoData.REMINDER_HOUR)
                shopping.setSchedule(listId, scheduledDate = date, reminderAt = date)
            }
            demo.items.forEach { item ->
                shopping.addItem(
                    ShoppingItem(
                        listId = listId,
                        name = item.name,
                        quantity = item.quantity,
                        categoryId = categoryIds[item.category],
                        price = item.price,
                        isChecked = item.bought
                    )
                )
            }
        }
        Log.i(TAG, "Seeded ${DemoData.lists.size} demo lists")
    }

    private fun dayAt(daysAhead: Int, hour: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, daysAhead)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private companion object {
        const val TAG = "DemoData"
    }
}

/** The screenshot demo content, in English (screenshots are taken with the English UI). */
private object DemoData {

    const val REMINDER_HOUR = 9

    class Item(
        val name: String,
        val category: String,
        val price: Double? = null,
        val quantity: String = "",
        val bought: Boolean = false
    )

    class DemoList(
        val name: String,
        val items: List<Item>,
        val budget: Double? = null,
        val daysAhead: Int? = null
    )

    // Created in this order; the newest list is shown first.
    val lists = listOf(
        DemoList(
            name = "Household",
            items = listOf(
                Item("Dish Soap", GroceryCatalog.CLEANING, 2.49),
                Item("Toilet Paper", GroceryCatalog.CLEANING, 6.99, quantity = "12 rolls", bought = true),
                Item("Trash Bags", GroceryCatalog.CLEANING, 4.29),
                Item("Paper Towels", GroceryCatalog.CLEANING, 3.99)
            )
        ),
        DemoList(
            name = "Weekend BBQ",
            daysAhead = 5,
            items = listOf(
                Item("Steak", GroceryCatalog.MEAT, 18.50, quantity = "1 kg"),
                Item("Sausage", GroceryCatalog.MEAT, 5.99),
                Item("Buns", GroceryCatalog.BAKERY, 2.99, bought = true),
                Item("Cola", GroceryCatalog.DRINKS, 3.49, quantity = "2 L"),
                Item("Chips", GroceryCatalog.SNACKS, 2.79),
                Item("Lemonade", GroceryCatalog.DRINKS, 2.99)
            )
        ),
        DemoList(
            name = "Weekly Groceries",
            budget = 40.0,
            daysAhead = 2,
            items = listOf(
                Item("Bananas", GroceryCatalog.PRODUCE, 2.49),
                Item("Tomatoes", GroceryCatalog.PRODUCE, 2.99, quantity = "500 g"),
                Item("Chicken", GroceryCatalog.MEAT, 8.99),
                Item("Coffee beans", GroceryCatalog.DRINKS, 7.99),
                Item("Eggs", GroceryCatalog.BREAKFAST, 3.49, quantity = "12"),
                Item("Milk", GroceryCatalog.DAIRY, 1.49, bought = true),
                Item("Sourdough bread", GroceryCatalog.BAKERY, 4.50, bought = true)
            )
        )
    )
}
