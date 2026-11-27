package com.listo.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class ItemEmojiTest {

    @Test
    fun `known items get their own picture`() {
        assertEquals("🍅", emojiForItem("Tomato"))
        assertEquals("🍗", emojiForItem("Chicken"))
        assertEquals("🥩", emojiForItem("Steak"))
        assertEquals("🐟", emojiForItem("Fish"))
        assertEquals("🍍", emojiForItem("Pineapple"))
    }

    @Test
    fun `matching ignores letter case`() {
        assertEquals("🥛", emojiForItem("milk"))
    }

    @Test
    fun `an unknown item gets its aisle's picture`() {
        assertEquals("🥩", emojiForItem("Something", GroceryCatalog.MEAT))
        assertEquals("🥬", emojiForItem("Something", GroceryCatalog.PRODUCE))
    }

    @Test
    fun `an unknown item without an aisle gets a basket`() {
        assertEquals(DEFAULT_ITEM_EMOJI, emojiForItem("Something"))
    }

    @Test
    fun `every aisle has its own picture`() {
        val pictures = GroceryCatalog.defaultCategories.map { (name, _) -> emojiForCategory(name) }
        assertEquals(pictures.size, pictures.toSet().size)
        assertTrue(DEFAULT_ITEM_EMOJI !in pictures)
    }

    @Test
    fun `every catalog item has a picture of its own`() {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File("src/main/res/values/catalog.xml"))
        val items = doc.getElementsByTagName("item")
        val keys = (0 until items.length).map { items.item(it).textContent.trim() }
        val missing = keys.filter { emojiForItem(it) == DEFAULT_ITEM_EMOJI }
        assertTrue("no picture for $missing", missing.isEmpty())
        // And not just the aisle fallback: each catalog key is mapped by name.
        val unmapped = keys.filterNot { hasOwnEmoji(it) }
        assertTrue("only an aisle picture for $unmapped", unmapped.isEmpty())
    }

    @Test
    fun `a plain meat item exists, so typing meat finds the meat aisle`() {
        assertNotEquals(DEFAULT_ITEM_EMOJI, emojiForItem("Meat"))
    }
}
