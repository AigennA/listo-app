package com.listo.app.utils

import java.util.Locale

data class GrocerySuggestion(val name: String, val category: String)

/**
 * Built-in catalog of common grocery items and default categories.
 * Used to seed starter categories, power type-ahead suggestions, and
 * auto-assign a category to freely typed items.
 */
object GroceryCatalog {

    const val PRODUCE = "Produce"
    const val DAIRY = "Dairy"
    const val MEAT = "Meat & Fish"
    const val BAKERY = "Bakery"
    const val DRINKS = "Drinks"
    const val SNACKS = "Snacks"
    const val CLEANING = "Cleaning"
    const val BREAKFAST = "Breakfast"
    const val PANTRY = "Pantry"

    /** name to hex color, used to seed categories on first launch. */
    val defaultCategories: List<Pair<String, String>> = listOf(
        PRODUCE to "#2ECC71",
        DAIRY to "#0984E3",
        MEAT to "#E17055",
        BAKERY to "#FDCB6E",
        DRINKS to "#6C5CE7",
        SNACKS to "#E84393",
        CLEANING to "#00B894",
        BREAKFAST to "#F39C12",
        PANTRY to "#A29BFE"
    )

    private val items: List<GrocerySuggestion> = buildList {
        fun group(category: String, vararg names: String) {
            names.forEach { add(GrocerySuggestion(it, category)) }
        }
        group(PRODUCE, "Tomato", "Cucumber", "Lettuce", "Pepper", "Potato", "Onion", "Garlic",
            "Carrot", "Apple", "Banana", "Orange", "Lemon", "Strawberry", "Grapes", "Watermelon",
            "Eggplant", "Spinach", "Parsley", "Avocado", "Mushroom")
        group(DAIRY, "Milk", "Yogurt", "Cheese", "Butter", "Cream", "Sour Cream", "Cottage Cheese")
        group(MEAT, "Chicken", "Ground Beef", "Steak", "Fish", "Salmon", "Sausage", "Meatballs",
            "Turkey", "Bacon", "Shrimp")
        group(BAKERY, "Bread", "Bagel", "Baguette", "Croissant", "Tortilla", "Toast Bread", "Buns")
        group(DRINKS, "Water", "Sparkling Water", "Cola", "Juice", "Tea", "Coffee", "Soda", "Lemonade")
        group(SNACKS, "Chips", "Chocolate", "Cookies", "Crackers", "Nuts", "Cake", "Wafer", "Gum", "Popcorn")
        group(CLEANING, "Detergent", "Dish Soap", "Bleach", "Soap", "Shampoo", "Toilet Paper",
            "Paper Towels", "Napkins", "Toothpaste", "Fabric Softener", "Glass Cleaner", "Trash Bags")
        group(BREAKFAST, "Eggs", "Honey", "Jam", "Olives", "Cereal", "Peanut Butter", "Oatmeal")
        group(PANTRY, "Rice", "Pasta", "Flour", "Sugar", "Salt", "Olive Oil", "Sunflower Oil",
            "Lentils", "Chickpeas", "Beans", "Tomato Paste", "Vinegar", "Spices", "Canned Tomatoes")
    }

    fun suggest(query: String, limit: Int = 6): List<GrocerySuggestion> {
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) return emptyList()
        val starts = items.filter { it.name.lowercase(Locale.ROOT).startsWith(q) }
        val contains = items.filter {
            val n = it.name.lowercase(Locale.ROOT)
            !n.startsWith(q) && n.contains(q)
        }
        return (starts + contains).distinctBy { it.name }.take(limit)
    }

    fun categoryFor(name: String): String? {
        val q = name.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) return null
        return items.firstOrNull { it.name.lowercase(Locale.ROOT) == q }?.category
            ?: items.firstOrNull { q.contains(it.name.lowercase(Locale.ROOT)) }?.category
    }
}
