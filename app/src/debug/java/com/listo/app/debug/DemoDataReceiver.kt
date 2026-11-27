package com.listo.app.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.listo.app.data.models.QuantityUnit
import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.models.StoreLayout
import com.listo.app.data.repository.CategoryRepository
import com.listo.app.data.repository.ShoppingRepository
import com.listo.app.data.repository.StoreRepository
import com.listo.app.domain.store.DefaultStoreLayout
import com.listo.app.domain.store.walk
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
 * Debug builds only: fills an empty install with the demo content used for the
 * store screenshots and README: lists, two stores with maps, and past trips
 * (archived, ticked in walking order) so Spending and History have content.
 *
 *   adb shell am broadcast -a com.listo.app.debug.SEED_DEMO -p com.mopilappar.listo.debug
 *
 * Does nothing if any active list already exists, so it never mixes with real data.
 */
@AndroidEntryPoint
class DemoDataReceiver : BroadcastReceiver() {

    @Inject lateinit var shopping: ShoppingRepository
    @Inject lateinit var categories: CategoryRepository
    @Inject lateinit var stores: StoreRepository
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
        val allCategories = categories.getAllCategories().first()
        val categoryIds = allCategories.associate { it.name to it.id }
        val aisles = DefaultStoreLayout.layout(allCategories)
        val walk = StoreLayout(aisles).walk()
        val storeIds = DemoData.stores.associate { (name, color) -> name to stores.createStore(name, color, aisles) }

        // Past trips first, ticked aisle by aisle along the map so no route change is suggested.
        DemoData.trips.forEach { trip ->
            val listId = shopping.createList(trip.name, storeId = storeIds[trip.store])
            val start = dayAt(-trip.daysAgo, DemoData.TRIP_HOUR)
            trip.items
                .sortedBy { walk.indexOf(categoryIds[it.category]).let { i -> if (i < 0) Int.MAX_VALUE else i } }
                .forEachIndexed { index, item ->
                    val at = start + index * DemoData.MINUTE
                    shopping.addItem(item.toShoppingItem(listId, categoryIds, checkedAt = at, createdAt = start))
                }
            shopping.archiveList(listId)
        }

        DemoData.lists.forEach { demo ->
            val listId = shopping.createList(demo.name, storeId = demo.store?.let(storeIds::get))
            demo.budget?.let { shopping.setBudget(listId, it) }
            demo.daysAhead?.let { days ->
                val date = dayAt(days, DemoData.REMINDER_HOUR)
                shopping.setSchedule(listId, scheduledDate = date, reminderAt = date)
            }
            demo.items.forEach { item ->
                val at = if (item.bought) System.currentTimeMillis() else null
                shopping.addItem(item.toShoppingItem(listId, categoryIds, checkedAt = at))
            }
        }
        Log.i(TAG, "Seeded ${DemoData.lists.size} demo lists and ${DemoData.trips.size} past trips")
    }

    private fun DemoData.Item.toShoppingItem(
        listId: Long,
        categoryIds: Map<String, Long>,
        checkedAt: Long?,
        createdAt: Long = System.currentTimeMillis()
    ) = ShoppingItem(
        listId = listId,
        name = name,
        categoryId = categoryIds[category],
        price = price,
        amount = amount,
        unit = unit,
        isChecked = checkedAt != null,
        checkedAt = checkedAt,
        createdAt = createdAt
    )

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
    const val TRIP_HOUR = 17
    const val MINUTE = 60_000L

    const val CITY_MARKET = "City Market"
    const val CORNER_SHOP = "Corner Shop"

    /** Store name to icon color. */
    val stores = listOf(CITY_MARKET to "#E17055", CORNER_SHOP to "#0984E3")

    class Item(
        val name: String,
        val category: String,
        val price: Double? = null,
        val amount: Double? = null,
        val unit: QuantityUnit? = null,
        val bought: Boolean = false
    )

    class DemoList(
        val name: String,
        val items: List<Item>,
        val budget: Double? = null,
        val daysAhead: Int? = null,
        val store: String? = null
    )

    /** A finished trip, archived to History; everything on it was bought. */
    class Trip(val name: String, val store: String, val daysAgo: Int, val items: List<Item>)

    // The first trip falls in the month before, so Spending has something to compare with.
    val trips = listOf(
        Trip(
            "Monthly stock-up", CITY_MARKET, daysAgo = 33, items = listOf(
                Item("Potatoes", GroceryCatalog.PRODUCE, 1.40, 2.0, QuantityUnit.KILO),
                Item("Chicken thighs", GroceryCatalog.MEAT, 11.80),
                Item("Salmon fillet", GroceryCatalog.MEAT, 15.90),
                Item("Butter", GroceryCatalog.DAIRY, 4.20),
                Item("Cheddar", GroceryCatalog.DAIRY, 9.80),
                Item("Coffee", GroceryCatalog.DRINKS, 8.90, 2.0, QuantityUnit.PACK),
                Item("Rice", GroceryCatalog.PANTRY, 3.60, 2.0, QuantityUnit.PACK),
                Item("Olive oil", GroceryCatalog.PANTRY, 9.50),
                Item("Dishwasher tabs", GroceryCatalog.CLEANING, 12.90),
                Item("Toilet paper", GroceryCatalog.CLEANING, 7.40, 2.0, QuantityUnit.PACK)
            )
        ),
        Trip(
            "Big shop", CITY_MARKET, daysAgo = 20, items = listOf(
                Item("Apples", GroceryCatalog.PRODUCE, 3.20, 1.0, QuantityUnit.KILO),
                Item("Rye bread", GroceryCatalog.BAKERY, 3.80),
                Item("Minced beef", GroceryCatalog.MEAT, 9.90),
                Item("Yogurt", GroceryCatalog.DAIRY, 2.40, 2.0, QuantityUnit.PIECE),
                Item("Pasta", GroceryCatalog.PANTRY, 1.60, 3.0, QuantityUnit.PACK),
                Item("Laundry detergent", GroceryCatalog.CLEANING, 8.90)
            )
        ),
        Trip(
            "Movie night", CORNER_SHOP, daysAgo = 13, items = listOf(
                Item("Popcorn", GroceryCatalog.SNACKS, 2.50, 2.0, QuantityUnit.PACK),
                Item("Chocolate", GroceryCatalog.SNACKS, 3.10),
                Item("Sparkling water", GroceryCatalog.DRINKS, 0.90, 4.0, QuantityUnit.PIECE)
            )
        ),
        Trip(
            "Weekly shop", CITY_MARKET, daysAgo = 7, items = listOf(
                Item("Carrots", GroceryCatalog.PRODUCE, 1.90, 1.0, QuantityUnit.KILO),
                Item("Croissants", GroceryCatalog.BAKERY, 4.20),
                Item("Salmon", GroceryCatalog.MEAT, 12.50),
                Item("Cheese", GroceryCatalog.DAIRY, 5.60),
                Item("Oats", GroceryCatalog.BREAKFAST, 2.30),
                Item("Orange juice", GroceryCatalog.DRINKS, 3.40, 2.0, QuantityUnit.PIECE)
            )
        )
    )

    // Created in this order; the newest list is shown first.
    val lists = listOf(
        DemoList(
            name = "Household",
            items = listOf(
                Item("Dish Soap", GroceryCatalog.CLEANING, 2.49),
                Item("Toilet Paper", GroceryCatalog.CLEANING, 6.99, 1.0, QuantityUnit.PACK, bought = true),
                Item("Trash Bags", GroceryCatalog.CLEANING, 4.29),
                Item("Paper Towels", GroceryCatalog.CLEANING, 3.99)
            )
        ),
        DemoList(
            name = "Weekend BBQ",
            daysAhead = 5,
            items = listOf(
                Item("Steak", GroceryCatalog.MEAT, 18.50, 1.0, QuantityUnit.KILO),
                Item("Sausage", GroceryCatalog.MEAT, 5.99),
                Item("Buns", GroceryCatalog.BAKERY, 2.99, bought = true),
                Item("Cola", GroceryCatalog.DRINKS, 1.75, 2.0, QuantityUnit.LITRE),
                Item("Chips", GroceryCatalog.SNACKS, 2.79),
                Item("Lemonade", GroceryCatalog.DRINKS, 2.99)
            )
        ),
        DemoList(
            name = "Weekly Groceries",
            store = CITY_MARKET,
            budget = 40.0,
            daysAhead = 2,
            items = listOf(
                Item("Bananas", GroceryCatalog.PRODUCE, 2.49),
                Item("Tomatoes", GroceryCatalog.PRODUCE, 5.98, 500.0, QuantityUnit.GRAM),
                Item("Chicken", GroceryCatalog.MEAT, 8.99),
                Item("Coffee beans", GroceryCatalog.DRINKS, 7.99),
                Item("Eggs", GroceryCatalog.BREAKFAST, 3.49, 1.0, QuantityUnit.PACK),
                Item("Milk", GroceryCatalog.DAIRY, 1.49, bought = true),
                Item("Sourdough bread", GroceryCatalog.BAKERY, 4.50, bought = true)
            )
        )
    )
}
