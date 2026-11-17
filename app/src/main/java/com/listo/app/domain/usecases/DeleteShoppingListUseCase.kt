package com.listo.app.domain.usecases

import com.listo.app.data.repository.ShoppingRepository
import javax.inject.Inject

class DeleteShoppingListUseCase @Inject constructor(private val repo: ShoppingRepository) {
    suspend operator fun invoke(id: Long) = repo.deleteList(id)
}
