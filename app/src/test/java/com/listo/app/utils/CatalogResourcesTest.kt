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
