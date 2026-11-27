package com.listo.app.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.ShoppingBasket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemIconsTest {

    @Test
    fun `an aisle gets its category's icon, a custom one the basket`() {
        assertEquals(Icons.Rounded.Grass, iconForCategory(GroceryCatalog.PRODUCE))
        assertEquals(Icons.Rounded.ShoppingBasket, iconForCategory("Pet food"))
    }

    @Test
    fun `every built-in aisle has its own icon`() {
        val icons = GroceryCatalog.defaultCategories.map { (category, _) -> iconForCategory(category) }
        assertEquals(icons.size, icons.toSet().size)
        assertTrue(Icons.Rounded.ShoppingBasket !in icons)
    }
}
