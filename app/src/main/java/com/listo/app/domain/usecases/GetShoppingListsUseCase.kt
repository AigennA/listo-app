package com.listo.app.domain.usecases

import com.listo.app.data.models.ShoppingList
import com.listo.app.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetShoppingListsUseCase @Inject constructor(private val repo: ShoppingRepository) {
    fun active(): Flow<List<ShoppingList>> = repo.getActiveLists()
    fun archived(): Flow<List<ShoppingList>> = repo.getArchivedLists()
    fun byId(id: Long): Flow<ShoppingList?> = repo.observeListById(id)
}
