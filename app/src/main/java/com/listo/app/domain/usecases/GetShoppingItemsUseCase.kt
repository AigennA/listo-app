package com.listo.app.domain.usecases

import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetShoppingItemsUseCase @Inject constructor(private val repo: ShoppingRepository) {
    fun forList(listId: Long): Flow<List<ShoppingItem>> = repo.getItemsForList(listId)

    /** The learned walking order (category ids) for [storeId]; empty without a store. */
    fun learnedRoute(storeId: Long?): Flow<List<Long>> =
        storeId?.let { repo.getLearnedRoute(it) } ?: flowOf(emptyList())
}
