package com.listo.app.utils

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CategoryLabelsTest {

    @Test
    fun `every built-in category has a translated label`() {
        GroceryCatalog.defaultCategories.forEach { (name, _) ->
            assertNotNull(name, CategoryLabels.labelRes(name))
        }
    }

    @Test
    fun `the uncategorised group has a translated label`() {
        assertNotNull(CategoryLabels.labelRes(GroceryCatalog.UNCATEGORIZED))
    }

    @Test
    fun `a user-created category keeps its own name`() {
        assertNull(CategoryLabels.labelRes("My weekend stuff"))
    }
}
