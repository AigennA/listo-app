package com.listo.app.domain.usecases

import com.listo.app.data.models.StoreLayout
import com.listo.app.data.repository.CategoryRepository
import com.listo.app.data.repository.StoreRepository
import com.listo.app.domain.store.DefaultStoreLayout
import com.listo.app.domain.store.StoreColors
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Adds a store with its own color and a ready-made map of every category. */
class AddStoreUseCase @Inject constructor(
    private val stores: StoreRepository,
    private val categories: CategoryRepository
) {
    /** Returns the new store's id, or null for a blank name. */
    suspend operator fun invoke(name: String): Long? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null
        val color = StoreColors.next(stores.getStoreColors())
        val map = DefaultStoreLayout.layout(categories.getAllCategories().first())
        return stores.createStore(trimmed, color, map)
    }
}

/**
 * Turns a typed store name into a store id: an existing store if the name is
 * already in My stores (case and spaces ignored), otherwise a new one.
 */
class ResolveStoreUseCase @Inject constructor(
    private val stores: StoreRepository,
    private val addStore: AddStoreUseCase
) {
    /** Returns null for a blank name. */
    suspend operator fun invoke(name: String): Long? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null
        val existing = stores.observeStores().first().firstOrNull { it.name.trim().equals(trimmed, ignoreCase = true) }
        return existing?.id ?: addStore(trimmed)
    }
}

/** Renames, recolors, re-maps and deletes stores. */
class UpdateStoreUseCase @Inject constructor(private val stores: StoreRepository) {

    suspend fun rename(id: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) stores.rename(id, trimmed)
    }

    suspend fun setColor(id: Long, color: String) = stores.setColor(id, color)

    suspend fun saveMap(id: Long, layout: StoreLayout) = stores.saveLayout(id, layout)

    suspend fun delete(id: Long) = stores.delete(id)
}
