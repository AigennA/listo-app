package com.listo.app.domain.usecases

import com.listo.app.data.models.ShoppingList
import com.listo.app.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Copies a list under [newName] with its budget and all items, every item
 * unticked so the copy is ready for the next shop. The date and reminder are
 * not copied: they belong to the original trip.
 */
class DuplicateShoppingListUseCase @Inject constructor(private val repo: ShoppingRepository) {
    suspend operator fun invoke(source: ShoppingList, newName: String): Long {
        val copyId = repo.createList(newName.trim())
        source.budget?.let { repo.setBudget(copyId, it) }
        repo.getItemsForList(source.id).first().forEach { item ->
            repo.addItem(item.copyTo(copyId))
        }
        return copyId
    }
}
