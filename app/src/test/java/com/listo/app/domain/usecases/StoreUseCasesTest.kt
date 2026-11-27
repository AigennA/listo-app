package com.listo.app.domain.usecases

import com.listo.app.data.models.Category
import com.listo.app.data.models.GridCell
import com.listo.app.data.models.Store
import com.listo.app.data.models.StoreLayout
import com.listo.app.data.repository.CategoryRepository
import com.listo.app.data.repository.ShoppingRepository
import com.listo.app.data.repository.StoreRepository
import com.listo.app.domain.store.DefaultStoreLayout
import com.listo.app.domain.store.StoreColors
import com.listo.app.utils.GroceryCatalog
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoreUseCasesTest {

    private val stores = mockk<StoreRepository>(relaxed = true)
    private val categoryRepo = mockk<CategoryRepository>()
    private val categories = listOf(
        Category(id = 1, name = GroceryCatalog.PRODUCE),
        Category(id = 2, name = GroceryCatalog.DAIRY)
    )

    private val addStore = AddStoreUseCase(stores, categoryRepo)

    init {
        every { categoryRepo.getAllCategories() } returns flowOf(categories)
        coEvery { stores.getStoreColors() } returns listOf(StoreColors.palette[0])
        coEvery { stores.createStore(any(), any(), any()) } returns 42L
    }

    @Test
    fun `adding a store trims its name and picks an unused color`() = runBlocking {
        val id = addStore("  Willys Tornby  ")

        assertEquals(42L, id)
        coVerify { stores.createStore("Willys Tornby", StoreColors.palette[1], any()) }
    }

    @Test
    fun `a new store starts with the default map of every category`() = runBlocking {
        addStore("ICA Maxi Tornby")

        coVerify { stores.createStore(any(), any(), DefaultStoreLayout.layout(categories)) }
    }

    @Test
    fun `a blank name adds nothing`() = runBlocking {
        assertNull(addStore("   "))
        coVerify(exactly = 0) { stores.createStore(any(), any(), any()) }
    }

    @Test
    fun `renaming trims the name and ignores a blank one`() = runBlocking {
        val update = UpdateStoreUseCase(stores)

        update.rename(7, "  Lidl  ")
        update.rename(7, "  ")

        coVerify(exactly = 1) { stores.rename(any(), any()) }
        coVerify { stores.rename(7, "Lidl") }
    }

    private val existing = listOf(Store(id = 5, name = "ICA Maxi Tornby", color = "#0984E3"))

    private fun resolver(): ResolveStoreUseCase {
        every { stores.observeStores() } returns flowOf(existing)
        return ResolveStoreUseCase(stores, addStore)
    }

    @Test
    fun `no store name means no store`() = runBlocking {
        assertNull(resolver()("  "))
    }

    @Test
    fun `a store already in My stores is reused, whatever the case`() = runBlocking {
        assertEquals(5L, resolver()("  ica maxi TORNBY "))
        coVerify(exactly = 0) { stores.createStore(any(), any(), any()) }
    }

    @Test
    fun `a new store name adds the store`() = runBlocking {
        assertEquals(42L, resolver()("Lidl Kungsängen"))
        coVerify { stores.createStore("Lidl Kungsängen", any(), any()) }
    }

    @Test
    fun `a new list is created with its store`() = runBlocking {
        val shopping = mockk<ShoppingRepository>()
        coEvery { shopping.createList(any(), any(), any()) } returns 9L

        assertEquals(9L, CreateShoppingListUseCase(shopping)("  Weekly ", storeId = 5L))
        coVerify { shopping.createList("Weekly", null, 5L) }
    }

    @Test
    fun `saving a map hands aisles, entrance and checkout to the repository`() = runBlocking {
        val update = UpdateStoreUseCase(stores)
        val map = StoreLayout(DefaultStoreLayout.layout(categories), entrance = GridCell(3, 0), checkout = GridCell(0, 8))

        update.saveMap(7, map)

        coVerify { stores.saveLayout(7, map) }
    }
}
