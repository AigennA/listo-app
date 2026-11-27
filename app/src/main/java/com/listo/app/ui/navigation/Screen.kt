package com.listo.app.ui.navigation

sealed class Screen(val route: String) {
    data object Splash      : Screen("splash")
    data object Onboarding  : Screen("onboarding")
    data object Home        : Screen("home")
    data object ListDetail  : Screen("list_detail/{listId}") {
        fun route(listId: Long) = "list_detail/$listId"
    }
    data object History     : Screen("history")
    data object Calendar    : Screen("calendar")
    data object Categories  : Screen("categories")
    data object CategoryItems : Screen("category_items/{categoryId}") {
        fun route(categoryId: Long) = "category_items/$categoryId"
    }
    data object Settings    : Screen("settings")

    /** My stores; with a list id it picks the store to shop that list in. */
    data object Stores : Screen("stores?listId={listId}") {
        fun route(listId: Long) = "stores?listId=$listId"
    }
    data object StoreMapEditor : Screen("store_map/{storeId}") {
        fun route(storeId: Long) = "store_map/$storeId"
    }
    data object StoreShopping : Screen("store_shop/{listId}/{storeId}") {
        fun route(listId: Long, storeId: Long) = "store_shop/$listId/$storeId"
    }
}
