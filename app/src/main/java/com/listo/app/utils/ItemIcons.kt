package com.listo.app.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

/**
 * Picks a small, relevant icon for a shopping item based on keywords in its
 * name. Falls back to a generic basket icon.
 */
fun iconForItem(name: String): ImageVector {
    val n = name.lowercase(Locale.ROOT)

    fun has(vararg keys: String) = keys.any { n.contains(it) }

    return when {
        has("water") -> Icons.Rounded.WaterDrop
        has("milk", "yogurt", "ayran", "kefir", "cream") -> Icons.Rounded.LocalDrink
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
        has("apple", "banana", "orange", "strawberry", "grape", "fruit", "lemon", "watermelon", "avocado") -> Icons.Rounded.Eco
        has("tomato", "cucumber", "lettuce", "pepper", "onion", "potato", "carrot", "vegetable", "salad", "spinach", "mushroom", "garlic", "eggplant") -> Icons.Rounded.Grass
        has("detergent", "soap", "clean", "bleach", "shampoo", "napkin", "paper towel", "toilet", "tissue", "toothpaste", "softener") -> Icons.Rounded.CleaningServices
        has("pet", "cat", "dog") -> Icons.Rounded.Pets
        has("medicine", "vitamin", "aspirin", "pill") -> Icons.Rounded.Medication
        has("baby", "diaper") -> Icons.Rounded.ChildCare
        has("flower") -> Icons.Rounded.LocalFlorist
        has("honey", "jam", "cereal", "oatmeal", "olive", "peanut butter") -> Icons.Rounded.FreeBreakfast
        else -> Icons.Rounded.ShoppingBasket
    }
}
