package com.listo.app.domain.usecases

import com.listo.app.data.repository.ShoppingRepository
import javax.inject.Inject

class UpdateShoppingListUseCase @Inject constructor(private val repo: ShoppingRepository) {
    suspend fun rename(id: Long, name: String) = repo.renameList(id, name.trim())
    suspend fun archive(id: Long) = repo.archiveList(id)
    suspend fun unarchive(id: Long) = repo.unarchiveList(id)
}
