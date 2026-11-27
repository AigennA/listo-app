package com.listo.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.listo.app.data.database.entities.StoreAisleEntity
import com.listo.app.data.database.entities.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    @Query("SELECT * FROM stores ORDER BY name COLLATE NOCASE ASC")
    fun observeStores(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE id = :id")
    fun observeStore(id: Long): Flow<StoreEntity?>

    @Query("SELECT color FROM stores")
    suspend fun getStoreColors(): List<String>

    @Insert
    suspend fun insertStore(store: StoreEntity): Long

    @Query("UPDATE stores SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("UPDATE stores SET color = :color WHERE id = :id")
    suspend fun setColor(id: Long, color: String)

    @Query("DELETE FROM stores WHERE id = :id")
    suspend fun deleteStoreRow(id: Long)

    @Query("UPDATE shopping_lists SET storeId = NULL WHERE storeId = :storeId")
    suspend fun unlinkLists(storeId: Long)

    /** Deletes a store and its map; lists shopped there stay, without a store. */
    @Transaction
    suspend fun deleteStore(id: Long) {
        unlinkLists(id)
        deleteStoreRow(id)
    }

    @Query("SELECT * FROM store_aisles WHERE storeId = :storeId")
    fun observeAisles(storeId: Long): Flow<List<StoreAisleEntity>>

    @Insert
    suspend fun insertAisles(aisles: List<StoreAisleEntity>)

    @Query("DELETE FROM store_aisles WHERE storeId = :storeId")
    suspend fun deleteAisles(storeId: Long)

    /** Saves a store's whole map at once, as the editor hands it over. */
    @Transaction
    suspend fun replaceAisles(storeId: Long, aisles: List<StoreAisleEntity>) {
        deleteAisles(storeId)
        insertAisles(aisles.map { it.copy(id = 0L, storeId = storeId) })
    }
}
