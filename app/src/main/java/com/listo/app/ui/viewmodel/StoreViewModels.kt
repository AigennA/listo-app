package com.listo.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.Category
import com.listo.app.data.models.ShoppingItem
import com.listo.app.data.models.Store
import com.listo.app.data.models.GridCell
import com.listo.app.data.models.StoreAisle
import com.listo.app.data.models.StoreLayout
import com.listo.app.data.repository.StoreRepository
import com.listo.app.domain.store.arrangedBy
import com.listo.app.domain.store.shouldOfferRoute
import com.listo.app.domain.usecases.AddShoppingItemUseCase
import com.listo.app.domain.usecases.AddStoreUseCase
import com.listo.app.domain.usecases.GetCategoriesUseCase
import com.listo.app.domain.usecases.GetShoppingItemsUseCase
import com.listo.app.domain.usecases.UpdateShoppingItemUseCase
import com.listo.app.domain.usecases.UpdateShoppingListUseCase
import com.listo.app.domain.usecases.UpdateStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Nav argument names shared by the store screens and their routes. */
object StoreArgs {
    const val STORE_ID = "storeId"
    const val LIST_ID = "listId"

    /** A list id meaning "opened from Settings, not from a list". */
    const val NO_LIST = -1L
}

data class StoresUiState(
    val stores: List<Store> = emptyList(),
    val isLoading: Boolean = true
)

/** My stores: add, rename, recolor and delete stores. */
@HiltViewModel
class StoresViewModel @Inject constructor(
    stores: StoreRepository,
    private val addStore: AddStoreUseCase,
    private val updateStore: UpdateStoreUseCase,
    private val updateList: UpdateShoppingListUseCase
) : ViewModel() {

    /** Remembers [storeId] as the store [listId] is shopped in. */
    fun linkList(listId: Long, storeId: Long) {
        viewModelScope.launch { updateList.setStore(listId, storeId) }
    }

    val uiState: StateFlow<StoresUiState> = stores.observeStores()
        .map { StoresUiState(stores = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StoresUiState())

    /** Adds a store and hands its id to [onAdded], e.g. to open its map. */
    fun add(name: String, onAdded: (Long) -> Unit) {
        viewModelScope.launch { addStore(name)?.let(onAdded) }
    }

    fun rename(id: Long, name: String) {
        viewModelScope.launch { updateStore.rename(id, name) }
    }

    fun setColor(id: Long, color: String) {
        viewModelScope.launch { updateStore.setColor(id, color) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { updateStore.delete(id) }
    }
}

/** A store map being edited. [saved] is what is stored, to spot unsaved changes. */
data class StoreMapEditorState(
    val aisles: List<StoreAisle> = emptyList(),
    val categories: List<Category> = emptyList(),
    val entrance: GridCell = StoreLayout.DEFAULT_ENTRANCE,
    val checkout: GridCell = StoreLayout.DEFAULT_CHECKOUT,
    val saved: StoreLayout = StoreLayout(aisles, entrance, checkout),
    val store: Store? = null,
    val isLoading: Boolean = false
) {
    val layout: StoreLayout get() = StoreLayout(aisles, entrance, checkout)

    /** Aisles with their category; aisles of deleted categories are left out. */
    val placed: List<Pair<Category, StoreAisle>>
        get() {
            val byId = categories.associateBy { it.id }
            return aisles.mapNotNull { aisle -> byId[aisle.categoryId]?.let { it to aisle } }
        }

    /** Categories that can still be put on the map. */
    val available: List<Category>
        get() {
            val onMap = aisles.map { it.categoryId }.toSet()
            return categories.filterNot { it.id in onMap }
        }

    val hasChanges: Boolean
        get() = aisles.toSet() != saved.aisles.toSet() || entrance != saved.entrance || checkout != saved.checkout
}

@HiltViewModel
class StoreMapEditorViewModel @Inject constructor(
    private val stores: StoreRepository,
    getCategories: GetCategoriesUseCase,
    private val updateStore: UpdateStoreUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: Long = savedStateHandle.get<Long>(StoreArgs.STORE_ID) ?: -1L

    private val _state = MutableStateFlow(StoreMapEditorState(isLoading = true))
    val state: StateFlow<StoreMapEditorState> = _state

    init {
        viewModelScope.launch {
            val aisles = stores.observeAisles(storeId).first()
            val store = stores.observeStore(storeId).first()
            val saved = StoreLayout(
                aisles,
                store?.entrance ?: StoreLayout.DEFAULT_ENTRANCE,
                store?.checkout ?: StoreLayout.DEFAULT_CHECKOUT
            )
            _state.update {
                it.copy(aisles = saved.aisles, entrance = saved.entrance, checkout = saved.checkout, saved = saved, isLoading = false)
            }
        }
        viewModelScope.launch {
            getCategories().collect { categories -> _state.update { it.copy(categories = categories) } }
        }
        viewModelScope.launch {
            stores.observeStore(storeId).collect { store -> _state.update { it.copy(store = store) } }
        }
    }

    fun move(categoryId: Long, x: Int, y: Int) = edit { it.moveAisle(categoryId, x, y) }

    fun moveEntrance(x: Int, y: Int) = edit { it.moveEntrance(x, y) }

    fun moveCheckout(x: Int, y: Int) = edit { it.moveCheckout(x, y) }

    fun add(categoryId: Long) = edit { it.addAisle(categoryId) }

    fun remove(categoryId: Long) = edit { it.removeAisle(categoryId) }

    /** Every change is saved at once, so leaving the editor never loses work. */
    private fun edit(change: (StoreLayout) -> StoreLayout) {
        _state.update {
            val next = change(it.layout)
            it.copy(aisles = next.aisles, entrance = next.entrance, checkout = next.checkout)
        }
        val layout = _state.value.layout
        viewModelScope.launch {
            updateStore.saveMap(storeId, layout)
            _state.update { it.copy(saved = layout) }
        }
    }
}

data class StoreShoppingUiState(
    val store: Store? = null,
    val map: StoreShoppingState = StoreShoppingState(),
    val isLoading: Boolean = true,
    /** The walking order learned for this store; offered when the map differs. */
    val learnedRoute: List<Long> = emptyList(),
    val offerRoute: Boolean = false
) {
    val layout: StoreLayout
        get() = StoreLayout(
            map.aisles,
            store?.entrance ?: StoreLayout.DEFAULT_ENTRANCE,
            store?.checkout ?: StoreLayout.DEFAULT_CHECKOUT
        )
}

/** Shopping a list aisle by aisle on a store map. */
@HiltViewModel
class StoreShoppingViewModel @Inject constructor(
    stores: StoreRepository,
    getItems: GetShoppingItemsUseCase,
    getCategories: GetCategoriesUseCase,
    private val updateItem: UpdateShoppingItemUseCase,
    private val addItemUseCase: AddShoppingItemUseCase,
    private val updateStore: UpdateStoreUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: Long = savedStateHandle.get<Long>(StoreArgs.STORE_ID) ?: -1L
    private val listId: Long = savedStateHandle.get<Long>(StoreArgs.LIST_ID) ?: StoreArgs.NO_LIST

    /** "Not now" on the route suggestion, for this visit. */
    private val routeDismissed = MutableStateFlow(false)

    private val mapState = combine(
        stores.observeStore(storeId),
        stores.observeAisles(storeId),
        getCategories(),
        getItems.forList(listId)
    ) { store, aisles, categories, items ->
        StoreShoppingUiState(
            store = store,
            map = StoreShoppingState(aisles = aisles, categories = categories, items = items),
            isLoading = false
        )
    }

    val uiState: StateFlow<StoreShoppingUiState> = combine(
        mapState,
        getItems.learnedRoute(storeId),
        routeDismissed
    ) { state, route, dismissed ->
        state.copy(learnedRoute = route, offerRoute = !dismissed && state.layout.shouldOfferRoute(route))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StoreShoppingUiState())

    /** Lays the map out along the learned route; entrance and checkout stay put. */
    fun applyRoute() {
        val state = uiState.value
        viewModelScope.launch { updateStore.saveMap(storeId, state.layout.arrangedBy(state.learnedRoute)) }
    }

    fun dismissRoute() { routeDismissed.value = true }

    fun toggle(item: ShoppingItem) {
        viewModelScope.launch { updateItem.toggle(item.id, !item.isChecked) }
    }

    /** One −/+ step on an item's amount while shopping. */
    fun step(item: ShoppingItem, up: Boolean) {
        viewModelScope.launch { updateItem(item.stepped(up)) }
    }

    /** Adds a forgotten item while shopping, into [aisleCategoryId] or the category its name suggests. */
    fun addItem(name: String, aisleCategoryId: Long?, guessedCategory: String?) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || listId == StoreArgs.NO_LIST) return
        val categoryId = uiState.value.map.categoryForNewItem(aisleCategoryId, guessedCategory)
        viewModelScope.launch {
            addItemUseCase(ShoppingItem(listId = listId, name = trimmed, categoryId = categoryId))
        }
    }
}
