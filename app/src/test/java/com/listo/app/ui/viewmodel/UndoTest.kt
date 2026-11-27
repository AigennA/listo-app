package com.listo.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.listo.app.data.models.ShoppingItem
import com.listo.app.domain.usecases.DeleteShoppingListUseCase
import com.listo.app.domain.usecases.GetShoppingListsUseCase
import com.listo.app.domain.usecases.UpdateShoppingItemUseCase
import com.listo.app.domain.usecases.UpdateShoppingListUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UndoTest {

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `a list in History can be moved back to My Lists`() {
        val lists = mockk<GetShoppingListsUseCase> { every { archived() } returns flowOf(emptyList()) }
        val update = mockk<UpdateShoppingListUseCase>(relaxed = true)
        val viewModel = HistoryViewModel(lists, mockk<DeleteShoppingListUseCase>(relaxed = true), update)

        viewModel.restoreList(7)

        coVerify { update.unarchive(7) }
    }

    @Test
    fun `undoing a tick puts the item back on the list`() {
        val updateItem = mockk<UpdateShoppingItemUseCase>(relaxed = true)
        val viewModel = ListDetailViewModel(
            getShoppingListsUseCase = mockk { every { byId(any()) } returns emptyFlow() },
            getShoppingItemsUseCase = mockk { every { forList(any()) } returns emptyFlow() },
            getCategoriesUseCase = mockk { every { this@mockk.invoke() } returns emptyFlow() },
            addItemUseCase = mockk(relaxed = true),
            updateItemUseCase = updateItem,
            deleteItemUseCase = mockk(relaxed = true),
            updateListUseCase = mockk(relaxed = true),
            deleteListUseCase = mockk(relaxed = true),
            savedStateHandle = SavedStateHandle(mapOf("listId" to 1L))
        )
        val milk = ShoppingItem(id = 3, listId = 1, name = "Milk", isChecked = true)

        viewModel.undoTick(milk)

        coVerify { updateItem.toggle(3, false) }
    }
}
