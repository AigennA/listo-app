package com.listo.app.utils

import java.util.Locale

/** Shown for an item Listo knows nothing about. */
const val DEFAULT_ITEM_EMOJI = "🛒"

/**
 * A picture for each catalog item, by its English key. Emoji come with the
 * phone, are in colour and need no image files. Items without an exact emoji
 * get the closest one.
 */
private val itemEmoji: Map<String, String> = mapOf(
    // Produce
    "tomato" to "🍅", "cucumber" to "🥒", "lettuce" to "🥬", "pepper" to "🫑", "potato" to "🥔",
    "onion" to "🧅", "garlic" to "🧄", "carrot" to "🥕", "apple" to "🍎", "banana" to "🍌",
    "orange" to "🍊", "lemon" to "🍋", "strawberry" to "🍓", "grapes" to "🍇", "watermelon" to "🍉",
    "eggplant" to "🍆", "spinach" to "🥬", "parsley" to "🌿", "avocado" to "🥑", "mushroom" to "🍄",
    "pineapple" to "🍍", "pear" to "🍐", "peach" to "🍑", "nectarine" to "🍑", "apricot" to "🍑",
    "plum" to "🫐", "cherry" to "🍒", "sour cherry" to "🍒", "melon" to "🍈", "kiwi" to "🥝",
    "mango" to "🥭", "pomegranate" to "🍎", "fig" to "🫐", "mandarin" to "🍊", "grapefruit" to "🍊",
    "lime" to "🍋", "raspberry" to "🍓", "blueberry" to "🫐", "blackberry" to "🫐", "quince" to "🍐",
    "coconut" to "🥥", "papaya" to "🥭", "passion fruit" to "🥭", "persimmon" to "🍅", "dates" to "🌴",
    "cabbage" to "🥬", "white cabbage" to "🥬", "red cabbage" to "🥬", "cauliflower" to "🥦",
    "broccoli" to "🥦", "zucchini" to "🥒", "pumpkin" to "🎃", "green onion" to "🌱", "red onion" to "🧅",
    "leek" to "🌱", "celery" to "🥬", "radish" to "🌶️", "beetroot" to "🫚", "green beans" to "🫛",
    "peas" to "🫛", "corn" to "🌽", "okra" to "🫛", "artichoke" to "🌿", "sweet potato" to "🍠",
    "ginger" to "🫚", "dill" to "🌿", "mint" to "🌿", "basil" to "🌿", "arugula" to "🥬",
    "coriander" to "🌿", "cherry tomatoes" to "🍅", "green pepper" to "🫑", "chili pepper" to "🌶️",
    "iceberg lettuce" to "🥬", "kale" to "🥬", "brussels sprouts" to "🥬", "asparagus" to "🌱",
    "purslane" to "🌿", "cress" to "🌱", "turnip" to "🥔", "fennel" to "🌿",
    // Dairy
    "milk" to "🥛", "yogurt" to "🥣", "cheese" to "🧀", "butter" to "🧈", "cream" to "🥛",
    "sour cream" to "🥣", "cottage cheese" to "🧀",
    // Meat & fish
    "meat" to "🥩", "chicken" to "🍗", "chicken breast" to "🍗", "ground beef" to "🥩", "steak" to "🥩",
    "lamb" to "🍖", "fish" to "🐟", "salmon" to "🐟", "tuna" to "🐟", "sausage" to "🌭",
    "meatballs" to "🧆", "turkey" to "🦃", "bacon" to "🥓", "shrimp" to "🦐",
    // Bakery
    "bread" to "🍞", "bagel" to "🥯", "baguette" to "🥖", "croissant" to "🥐", "tortilla" to "🫓",
    "toast bread" to "🍞", "buns" to "🍔",
    // Drinks
    "water" to "💧", "sparkling water" to "🫧", "cola" to "🥤", "juice" to "🧃", "tea" to "🍵",
    "coffee" to "☕", "soda" to "🥤", "lemonade" to "🍋",
    // Snacks
    "chips" to "🍟", "chocolate" to "🍫", "cookies" to "🍪", "crackers" to "🍘", "nuts" to "🥜",
    "cake" to "🍰", "wafer" to "🧇", "gum" to "🍬", "popcorn" to "🍿",
    // Cleaning
    "detergent" to "🧴", "dish soap" to "🧽", "bleach" to "🧴", "soap" to "🧼", "shampoo" to "🧴",
    "toilet paper" to "🧻", "paper towels" to "🧻", "napkins" to "🧻", "toothpaste" to "🪥",
    "fabric softener" to "🧺", "glass cleaner" to "🧽", "trash bags" to "🗑️",
    // Breakfast
    "eggs" to "🥚", "honey" to "🍯", "jam" to "🍓", "olives" to "🫒", "cereal" to "🥣",
    "peanut butter" to "🥜", "oatmeal" to "🥣",
    // Pantry
    "rice" to "🍚", "pasta" to "🍝", "flour" to "🌾", "sugar" to "🧂", "salt" to "🧂",
    "olive oil" to "🫒", "sunflower oil" to "🌻", "lentils" to "🫘", "chickpeas" to "🫘",
    "beans" to "🫘", "tomato paste" to "🥫", "vinegar" to "🍶", "spices" to "🌶️",
    "canned tomatoes" to "🥫"
)

/** One picture per built-in aisle, for items not in [itemEmoji]. */
private val categoryEmoji: Map<String, String> = mapOf(
    GroceryCatalog.PRODUCE to "🥬",
    GroceryCatalog.DAIRY to "🧀",
    GroceryCatalog.MEAT to "🥩",
    GroceryCatalog.BAKERY to "🍞",
    GroceryCatalog.DRINKS to "🥤",
    GroceryCatalog.SNACKS to "🍫",
    GroceryCatalog.CLEANING to "🧴",
    GroceryCatalog.BREAKFAST to "🍳",
    GroceryCatalog.PANTRY to "🥫"
)

private fun String.emojiKey(): String = trim().lowercase(Locale.ROOT)

/** True when [key] has a picture of its own, not just its aisle's. */
fun hasOwnEmoji(key: String): Boolean = key.emojiKey() in itemEmoji

/** Picture of a built-in aisle, or the basket for an aisle the user made. */
fun emojiForCategory(category: String?): String = categoryEmoji[category] ?: DEFAULT_ITEM_EMOJI

/**
 * Picture for an item by its English catalog [key]; an unknown item gets the
 * picture of its [category], and without one the basket.
 */
fun emojiForItem(key: String, category: String? = null): String =
    itemEmoji[key.emojiKey()] ?: emojiForCategory(category)
