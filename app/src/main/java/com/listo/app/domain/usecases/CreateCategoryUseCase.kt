package com.listo.app.domain.usecases

import com.listo.app.data.models.Category
import com.listo.app.data.repository.CategoryRepository
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(private val repo: CategoryRepository) {
    suspend operator fun invoke(category: Category): Long = repo.insertCategory(category)
    suspend fun delete(id: Long) = repo.deleteCategoryById(id)
}
