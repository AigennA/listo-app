package com.listo.app.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

/** One icon per built-in category, for items the keywords below don't know. */
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

/** Icon for a whole aisle; categories the user made get the basket. */
fun iconForCategory(category: String): ImageVector = categoryIcons[category] ?: Icons.Rounded.ShoppingBasket

/**
 * Picks a small, relevant icon for a shopping item based on keywords in its
 * English [name]. An unknown item gets the icon of its [category], so items in
 * the same aisle look alike; without one it gets a generic basket.
 */
fun iconForItem(name: String, category: String? = null): ImageVector {
    val n = name.lowercase(Locale.ROOT)

    fun has(vararg keys: String) = keys.any { n.contains(it) }

    return when {
        has("water") -> Icons.Rounded.WaterDrop
        has("milk", "yogurt", "ayran", "kefir", "cream") -> Icons.Rounded.LocalDrink
        // Before coffee/tea: "steak" contains the letters "tea".
        has("steak") -> Icons.Rounded.LunchDining
        has("coffee", "tea") -> Icons.Rounded.LocalCafe
        has("wine", "beer", "vodka", "whisky", "whiskey", "liquor", "alcohol") -> Icons.Rounded.WineBar
        has("cola", "soda", "juice", "lemonade", "drink") -> Icons.Rounded.LocalBar
        has("bread", "bagel", "baguette", "croissant", "bun", "toast", "tortilla") -> Icons.Rounded.BakeryDining
        has("cake", "cookie", "biscuit", "chocolate", "wafer", "candy") -> Icons.Rounded.Cake
        has("ice cream", "icecream") -> Icons.Rounded.Icecream
        has("egg") -> Icons.Rounded.Egg
        has("fish", "salmon", "shrimp", "seafood", "tuna") -> Icons.Rounded.SetMeal
        has("chicken", "beef", "steak", "meat", "sausage", "bacon", "turkey", "meatball") -> Icons.Rounded.LunchDining
        has("cheese", "butter") -> Icons.Rounded.Egg
        has("pizza") -> Icons.Rounded.LocalPizza
        has("pasta", "noodle", "rice", "lentil", "chickpea", "bean", "flour", "spaghetti") -> Icons.Rounded.RamenDining
        has(
            "apple", "banana", "orange", "strawberry", "grape", "fruit", "lemon", "melon", "avocado",
            "pear", "peach", "nectarine", "apricot", "plum", "cherry", "kiwi", "mango", "fig", "mandarin",
            "lime", "berry", "quince", "coconut", "papaya", "persimmon", "dates"
        ) -> Icons.Rounded.Eco
        has(
            "tomato", "cucumber", "lettuce", "pepper", "onion", "potato", "carrot", "vegetable", "salad",
            "spinach", "mushroom", "garlic", "eggplant", "cabbage", "cauliflower", "broccoli", "zucchini",
            "pumpkin", "leek", "celery", "radish", "beetroot", "peas", "okra", "artichoke", "ginger", "dill",
            "mint", "basil", "arugula", "coriander", "kale", "sprouts", "asparagus", "purslane", "cress",
            "turnip", "fennel"
        ) -> Icons.Rounded.Grass
        has("detergent", "soap", "clean", "bleach", "shampoo", "napkin", "paper towel", "toilet", "tissue", "toothpaste", "softener") -> Icons.Rounded.CleaningServices
        has("pet", "cat", "dog") -> Icons.Rounded.Pets
        has("medicine", "vitamin", "aspirin", "pill") -> Icons.Rounded.Medication
        has("baby", "diaper") -> Icons.Rounded.ChildCare
        has("flower") -> Icons.Rounded.LocalFlorist
        has("honey", "jam", "cereal", "oatmeal", "olive", "peanut butter") -> Icons.Rounded.FreeBreakfast
        else -> categoryIcons[category] ?: Icons.Rounded.ShoppingBasket
    }
}
