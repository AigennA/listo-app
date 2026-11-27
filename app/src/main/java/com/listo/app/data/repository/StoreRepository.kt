package com.listo.app.data.repository

import com.listo.app.data.database.dao.StoreDao
import com.listo.app.data.database.entities.StoreEntity
import com.listo.app.data.database.entities.toDomain
import com.listo.app.data.database.entities.toEntity
import com.listo.app.data.models.Store
import com.listo.app.data.models.StoreAisle
import com.listo.app.data.models.StoreLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Stores and their aisle maps. Everything stays on the device. */
@Singleton
class StoreRepository @Inject constructor(private val storeDao: StoreDao) {

    fun observeStores(): Flow<List<Store>> =
        storeDao.observeStores().map { rows -> rows.map { it.toDomain() } }

    fun observeStore(id: Long): Flow<Store?> = storeDao.observeStore(id).map { it?.toDomain() }

    fun observeAisles(storeId: Long): Flow<List<StoreAisle>> =
        storeDao.observeAisles(storeId).map { rows -> rows.map { it.toDomain() } }

    suspend fun getStoreColors(): List<String> = storeDao.getStoreColors()

    suspend fun createStore(name: String, color: String, aisles: List<StoreAisle>): Long {
        val id = storeDao.insertStore(StoreEntity(name = name, color = color))
        storeDao.replaceAisles(id, aisles.map { it.toEntity(id) })
        return id
    }

    suspend fun rename(id: Long, name: String) = storeDao.rename(id, name)

    suspend fun setColor(id: Long, color: String) = storeDao.setColor(id, color)

    /** Saves a whole map at once: aisles, entrance and checkout. */
    suspend fun saveLayout(storeId: Long, layout: StoreLayout) = storeDao.saveLayout(
        storeId,
        layout.aisles.map { it.toEntity(storeId) },
        layout.entrance,
        layout.checkout
    )

    suspend fun delete(id: Long) = storeDao.deleteStore(id)
}
