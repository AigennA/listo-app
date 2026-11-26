package com.listo.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * The item catalog pairs each translated name with its English key by position,
 * so every language must list the same number of items per category, and
 * every language must translate every built-in category name.
 */
class CatalogResourcesTest {

    private val resDir = File("src/main/res")
    private val translatedDirs = listOf("values-sv", "values-tr")

    private fun resources(dir: String): List<Element> {
        val files = File(resDir, dir).listFiles { f -> f.extension == "xml" }.orEmpty()
        return files.flatMap { file ->
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            val nodes = doc.documentElement.childNodes
            (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }
        }
    }

    private fun arraySizes(dir: String): Map<String, Int> =
        resources(dir)
            .filter { it.tagName == "string-array" && it.getAttribute("name").startsWith("catalog_") }
            .associate { it.getAttribute("name") to it.getElementsByTagName("item").length }

    private fun stringNames(dir: String): Set<String> =
        resources(dir).filter { it.tagName == "string" }.map { it.getAttribute("name") }.toSet()

    @Test
    fun `the default language defines a catalog array for every category`() {
        val sizes = arraySizes("values")
        assertEquals(GroceryCatalog.defaultCategories.size, sizes.size)
        assertTrue(sizes.values.all { it > 0 })
    }

    @Test
    fun `every translation lists the same number of items per category`() {
        val expected = arraySizes("values")
        translatedDirs.forEach { dir -> assertEquals(dir, expected, arraySizes(dir)) }
    }

    /** Catalog arrays of [dir] keyed by category, as the app pairs them. */
    private fun catalogArrays(dir: String): Map<String, List<String>> {
        val categoryByArray = mapOf(
            "catalog_produce" to GroceryCatalog.PRODUCE,
            "catalog_dairy" to GroceryCatalog.DAIRY,
            "catalog_meat" to GroceryCatalog.MEAT,
            "catalog_bakery" to GroceryCatalog.BAKERY,
            "catalog_drinks" to GroceryCatalog.DRINKS,
            "catalog_snacks" to GroceryCatalog.SNACKS,
            "catalog_cleaning" to GroceryCatalog.CLEANING,
            "catalog_breakfast" to GroceryCatalog.BREAKFAST,
            "catalog_pantry" to GroceryCatalog.PANTRY
        )
        return resources(dir)
            .filter { it.tagName == "string-array" && it.getAttribute("name") in categoryByArray }
            .associate { array ->
                val items = array.getElementsByTagName("item")
                categoryByArray.getValue(array.getAttribute("name")) to
                    (0 until items.length).map { items.item(it).textContent.trim() }
            }
    }

    private fun catalog(dir: String) = ItemCatalog.of(catalogArrays("values"), catalogArrays(dir))

    @Test
    fun `english keys are unique across the catalog`() {
        val keys = catalogArrays("values").values.flatten()
        val duplicates = keys.groupBy { it.lowercase() }.filterValues { it.size > 1 }.keys
        assertTrue("duplicate keys: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun `the produce section covers the common fruit and vegetables`() {
        assertTrue(catalogArrays("values").getValue(GroceryCatalog.PRODUCE).size >= 60)
    }

    @Test
    fun `turkish tester items land in produce`() {
        val tr = catalog("values-tr")
        listOf("Bal kabağı", "Beyaz lahana", "Yeşil soğan", "Ananas", "annas").forEach {
            assertEquals(it, GroceryCatalog.PRODUCE, tr.categoryFor(it))
        }
    }

    @Test
    fun `turkish names match whatever the letter case`() {
        val tr = catalog("values-tr")
        listOf("BALKABAĞI", "bAl KaBaK", "İNCİR", "incir", "ISPANAK", "ıspanak", "Beyaz LAHANA").forEach {
            assertEquals(it, GroceryCatalog.PRODUCE, tr.categoryFor(it))
        }
        assertEquals("Ananas", tr.suggest("ANA").first().name)
    }

    @Test
    fun `typing white cabbage in turkish suggests it`() {
        assertEquals("Beyaz Lahana", catalog("values-tr").suggest("beyaz la").first().name)
    }

    @Test
    fun `swedish and english fruit and vegetables land in produce`() {
        listOf("Ananas", "Vitkål", "Pumpa", "Salladslök").forEach {
            assertEquals(it, GroceryCatalog.PRODUCE, catalog("values-sv").categoryFor(it))
        }
        listOf("Pineapple", "White cabbage", "Pumpkin", "Green onion").forEach {
            assertEquals(it, GroceryCatalog.PRODUCE, catalog("values").categoryFor(it))
        }
    }

    @Test
    fun `every translation names every built-in category`() {
        val labels = stringNames("values").filter { it.startsWith("category_builtin_") || it == "category_other" }
        assertTrue(labels.isNotEmpty())
        translatedDirs.forEach { dir ->
            val missing = labels - stringNames(dir)
            assertTrue("$dir is missing $missing", missing.isEmpty())
        }
    }
}
