package com.listo.app.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.LunchDining
import androidx.compose.material.icons.rounded.ShoppingBasket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemIconsTest {

    @Test
    fun `steak gets a meat icon, not a tea cup`() {
        assertEquals(Icons.Rounded.LunchDining, iconForItem("Steak"))
    }

    @Test
    fun `tea still gets a cup`() {
        assertEquals(Icons.Rounded.LocalCafe, iconForItem("Tea"))
    }

    @Test
    fun `an unknown item gets its category's icon`() {
        assertEquals(Icons.Rounded.Grass, iconForItem("Something green", GroceryCatalog.PRODUCE))
        assertEquals(Icons.Rounded.LocalDrink, iconForItem("Something white", GroceryCatalog.DAIRY))
        assertEquals(Icons.Rounded.CleaningServices, iconForItem("Something", GroceryCatalog.CLEANING))
    }

    @Test
    fun `an unknown item without a category gets the basket`() {
        assertEquals(Icons.Rounded.ShoppingBasket, iconForItem("Something"))
    }

    @Test
    fun `every built-in category has its own icon`() {
        val icons = GroceryCatalog.defaultCategories.map { (category, _) -> iconForItem("zzz", category) }
        assertEquals(icons.size, icons.toSet().size)
        assertTrue(Icons.Rounded.ShoppingBasket !in icons)
    }

    @Test
    fun `new fruit and vegetables get produce icons`() {
        assertEquals(Icons.Rounded.Eco, iconForItem("Pineapple"))
        assertEquals(Icons.Rounded.Grass, iconForItem("White Cabbage"))
    }
}
