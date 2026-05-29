package com.listo.app.domain.usecases

import com.listo.app.data.models.Category
import com.listo.app.data.repository.CategoryRepository
import com.listo.app.utils.GroceryCatalog
import javax.inject.Inject

/** Inserts the built-in starter categories. Call once on first launch. */
class SeedCategoriesUseCase @Inject constructor(private val repo: CategoryRepository) {
    suspend operator fun invoke() {
        GroceryCatalog.defaultCategories.forEach { (name, color) ->
            repo.insertCategory(Category(name = name, color = color))
        }
    }
}
