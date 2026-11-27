package com.listo.app.domain.usecases

import com.listo.app.data.repository.ShoppingRepository
import javax.inject.Inject

class CreateShoppingListUseCase @Inject constructor(private val repo: ShoppingRepository) {
    suspend operator fun invoke(name: String, scheduledDate: Long? = null, storeId: Long? = null): Long =
        repo.createList(name.trim(), scheduledDate, storeId)
}
