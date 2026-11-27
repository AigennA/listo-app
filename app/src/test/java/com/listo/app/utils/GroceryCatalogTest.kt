package com.listo.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GroceryCatalogTest {

    private val english = mapOf(
        GroceryCatalog.DAIRY to listOf("Milk", "Cheese", "Butter"),
        GroceryCatalog.PRODUCE to listOf("Tomato", "Apple"),
        GroceryCatalog.DRINKS to listOf("Tea", "Coffee")
    )

    // Stand-in translations: the catalog only cares that names differ from the keys.
    private val localized = mapOf(
        GroceryCatalog.DAIRY to listOf("Mlk-L", "Chs-L", "Btr-L"),
        GroceryCatalog.PRODUCE to listOf("Tmt-L", "Apl-L"),
        GroceryCatalog.DRINKS to listOf("Te", "Cof-L")
    )

    private val catalog = ItemCatalog.of(english, localized)

    @Test
    fun `suggest matches the localized names`() {
        assertEquals(listOf("Mlk-L"), catalog.suggest("mlk").map { it.name })
    }

    @Test
    fun `suggest with blank query is empty`() {
        assertTrue(catalog.suggest("   ").isEmpty())
    }

    @Test
    fun `suggest respects the limit`() {
        assertTrue(catalog.suggest("-l", limit = 2).size <= 2)
    }

    @Test
    fun `suggestions carry their category key`() {
        assertEquals(GroceryCatalog.DAIRY, catalog.suggest("chs").single().category)
    }

    @Test
    fun `categoryFor maps a localized name`() {
        assertEquals(GroceryCatalog.DAIRY, catalog.categoryFor("mlk-l"))
    }

    @Test
    fun `categoryFor still maps an english name`() {
        assertEquals(GroceryCatalog.DAIRY, catalog.categoryFor("Milk"))
    }

    @Test
    fun `categoryFor matches a known name at the start of a word`() {
        assertEquals(GroceryCatalog.PRODUCE, catalog.categoryFor("green apple"))
    }

    @Test
    fun `categoryFor ignores a short name hidden inside another word`() {
        // "Te" (tea) must not claim words that merely contain the letters "te".
        assertNull(catalog.categoryFor("pasted"))
    }

    @Test
    fun `categoryFor is null for an unknown item`() {
        assertNull(catalog.categoryFor("zzzznotathing"))
    }

    @Test
    fun `keyFor returns the english key of a localized name`() {
        assertEquals("Milk", catalog.keyFor("Mlk-L"))
    }

    @Test
    fun `keyFor is null for a free-typed name`() {
        assertNull(catalog.keyFor("Something else"))
    }

    @Test
    fun `itemsForCategory returns localized names`() {
        assertEquals(listOf("Tmt-L", "Apl-L"), catalog.itemsForCategory(GroceryCatalog.PRODUCE).map { it.name })
    }

    @Test
    fun `a category with a mismatched translation falls back to english`() {
        val broken = ItemCatalog.of(english, localized + (GroceryCatalog.PRODUCE to listOf("Only one")))
        assertEquals(listOf("Tomato", "Apple"), broken.itemsForCategory(GroceryCatalog.PRODUCE).map { it.name })
    }

    // A small catalog with real-looking names for matching rules.
    private val words = ItemCatalog.of(
        english = mapOf(
            GroceryCatalog.PRODUCE to listOf("Pineapple", "Pumpkin", "Onion", "Green Onion"),
            GroceryCatalog.BREAKFAST to listOf("Honey"),
            GroceryCatalog.DAIRY to listOf("Milk")
        ),
        localized = mapOf(
            GroceryCatalog.PRODUCE to listOf("Ananas", "Bal Kabağı", "Soğan", "Yeşil Soğan"),
            GroceryCatalog.BREAKFAST to listOf("Bal"),
            GroceryCatalog.DAIRY to listOf("Süt")
        )
    )

    @Test
    fun `suggest waits for two letters`() {
        assertTrue(words.suggest("a").isEmpty())
        assertEquals("Ananas", words.suggest("an").first().name)
    }

    @Test
    fun `suggest tolerates a typo`() {
        assertEquals("Ananas", words.suggest("annas").first().name)
    }

    @Test
    fun `suggest ignores turkish letters the user did not type`() {
        assertEquals("Soğan", words.suggest("sogan").first().name)
    }

    @Test
    fun `suggest puts an exact match first`() {
        assertEquals("Bal", words.suggest("bal").first().name)
    }

    @Test
    fun `categoryFor prefers the longest known name`() {
        // "Bal Kabağı" (pumpkin) must not be taken for "Bal" (honey).
        assertEquals(GroceryCatalog.PRODUCE, words.categoryFor("Bal kabağı"))
        assertEquals(GroceryCatalog.PRODUCE, words.categoryFor("organik bal kabağı"))
    }

    @Test
    fun `categoryFor still finds the shorter name on its own`() {
        assertEquals(GroceryCatalog.BREAKFAST, words.categoryFor("çiçek bal"))
    }

    @Test
    fun `categoryFor ignores spaces the user left out`() {
        assertEquals(GroceryCatalog.PRODUCE, words.categoryFor("Balkabağı"))
        assertEquals(GroceryCatalog.PRODUCE, words.categoryFor("balkabagi"))
        assertEquals(GroceryCatalog.PRODUCE, words.categoryFor("yesilsogan"))
    }

    @Test
    fun `suggest ignores spaces the user left out`() {
        assertEquals("Bal Kabağı", words.suggest("balkab").first().name)
    }

    @Test
    fun `keyFor finds the item the same way categoryFor does`() {
        assertEquals("Pumpkin", words.keyFor("organik BAL KABAĞI"))
        assertEquals("Pumpkin", words.keyFor("balkabagi"))
        assertEquals("Pineapple", words.keyFor("annas"))
        assertEquals("Honey", words.keyFor("çiçek bal"))
    }

    @Test
    fun `categoryFor understands a typo`() {
        assertEquals(GroceryCatalog.PRODUCE, words.categoryFor("annas"))
    }

    @Test
    fun `categoryFor understands a name typed without turkish letters`() {
        assertEquals(GroceryCatalog.PRODUCE, words.categoryFor("yesil sogan"))
        assertEquals(GroceryCatalog.PRODUCE, words.categoryFor("bal kabagi"))
    }

    @Test
    fun `categoryFor does not guess on very short words`() {
        assertNull(words.categoryFor("sit"))
    }

    // An English catalog that also knows the Turkish names.
    private val multilingual = ItemCatalog.of(
        english = mapOf(GroceryCatalog.MEAT to listOf("Meat", "Chicken")),
        localized = mapOf(GroceryCatalog.MEAT to listOf("Meat", "Chicken")),
        otherLanguages = listOf(mapOf(GroceryCatalog.MEAT to listOf("Et", "Tavuk")))
    )

    @Test
    fun `a name in another supported language is still understood`() {
        assertEquals(GroceryCatalog.MEAT, multilingual.categoryFor("Tavuk"))
        assertEquals("Chicken", multilingual.keyFor("tavuk"))
        assertEquals("Meat", multilingual.keyFor("Et"))
    }

    @Test
    fun `a very short name only matches as a whole word`() {
        val short = ItemCatalog.of(
            english = mapOf(GroceryCatalog.PANTRY to listOf("Flour"), GroceryCatalog.MEAT to listOf("Meat")),
            localized = mapOf(GroceryCatalog.PANTRY to listOf("Un"), GroceryCatalog.MEAT to listOf("Et"))
        )
        assertNull(short.categoryFor("Unicorn"))
        assertNull(short.categoryFor("Ethanol"))
        assertEquals(GroceryCatalog.PANTRY, short.categoryFor("tam un"))
        assertEquals(GroceryCatalog.MEAT, short.categoryFor("dana et"))
    }

    @Test
    fun `suggestions stay in the app's language`() {
        assertTrue(multilingual.suggest("ta").isEmpty())
        assertEquals("Chicken", multilingual.suggest("chi").single().name)
    }

    @Test
    fun `default categories are non-empty and unique`() {
        val names = GroceryCatalog.defaultCategories.map { it.first }
        assertTrue(names.isNotEmpty())
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun `every default category has an item list resource`() {
        GroceryCatalog.defaultCategories.forEach { (name, _) ->
            assertNotNull(name, GroceryCatalog.itemArrays[name])
        }
    }
}
