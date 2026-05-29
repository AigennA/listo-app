package com.listo.app.domain.usecases

import com.listo.app.data.models.Category
import com.listo.app.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(private val repo: CategoryRepository) {
    operator fun invoke(): Flow<List<Category>> = repo.getAllCategories()
}
