package com.listo.app.domain.usecases

import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.repository.ShoppingRepository
import javax.inject.Inject

class AddShoppingItemUseCase @Inject constructor(private val repo: ShoppingRepository) {
    suspend operator fun invoke(item: ShoppingItem): Long = repo.addItem(item)
}
