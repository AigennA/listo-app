package com.listo.app.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

/** One icon per built-in category, drawn in white on the aisle tiles of a store map. */
private val categoryIcons: Map<String, ImageVector> by lazy {
    mapOf(
        GroceryCatalog.PRODUCE to Icons.Rounded.Grass,
        GroceryCatalog.DAIRY to Icons.Rounded.LocalDrink,
        GroceryCatalog.MEAT to Icons.Rounded.LunchDining,
        GroceryCatalog.BAKERY to Icons.Rounded.BakeryDining,
        GroceryCatalog.DRINKS to Icons.Rounded.LocalBar,
        GroceryCatalog.SNACKS to Icons.Rounded.Cake,
        GroceryCatalog.CLEANING to Icons.Rounded.CleaningServices,
        GroceryCatalog.BREAKFAST to Icons.Rounded.FreeBreakfast,
        GroceryCatalog.PANTRY to Icons.Rounded.Kitchen
    )
}

/** Icon for a whole aisle; categories the user made get the basket. Items use [emojiForItem]. */
fun iconForCategory(category: String): ImageVector = categoryIcons[category] ?: Icons.Rounded.ShoppingBasket
