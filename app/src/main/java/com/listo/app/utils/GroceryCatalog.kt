package com.listo.app.utils

import com.listo.app.R

/**
 * Keys of the built-in categories. They are stored in the database in English
 * and only translated for display (see [CategoryLabels]), so existing data
 * keeps working whatever language the app runs in.
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

    /** Group key for items without a category. */
    const val UNCATEGORIZED = "Other"

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

    /** Category key to the string-array resource listing its common items. */
    val itemArrays: Map<String, Int> = mapOf(
        PRODUCE to R.array.catalog_produce,
        DAIRY to R.array.catalog_dairy,
        MEAT to R.array.catalog_meat,
        BAKERY to R.array.catalog_bakery,
        DRINKS to R.array.catalog_drinks,
        SNACKS to R.array.catalog_snacks,
        CLEANING to R.array.catalog_cleaning,
        BREAKFAST to R.array.catalog_breakfast,
        PANTRY to R.array.catalog_pantry
    )
}
