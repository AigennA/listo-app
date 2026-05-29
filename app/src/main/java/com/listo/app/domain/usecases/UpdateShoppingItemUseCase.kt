package com.listo.app.domain.usecases

import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.repository.ShoppingRepository
import javax.inject.Inject

class UpdateShoppingItemUseCase @Inject constructor(private val repo: ShoppingRepository) {
    suspend operator fun invoke(item: ShoppingItem) = repo.updateItem(item)
    suspend fun toggle(id: Long, checked: Boolean) = repo.toggleItem(id, checked)
}
