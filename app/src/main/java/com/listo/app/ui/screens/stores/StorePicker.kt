package com.listo.app.ui.screens.stores

import com.listo.app.data.models.Store

/** Matching rules for picking a store by typing its name. */
object StorePicker {

    /** Stores whose name contains [query]; all of them for an empty query. */
    fun matches(stores: List<Store>, query: String): List<Store> {
        val q = query.trim()
        return if (q.isEmpty()) stores else stores.filter { it.name.contains(q, ignoreCase = true) }
    }

    /** True when [query] names a store not yet in My stores. */
    fun canAdd(stores: List<Store>, query: String): Boolean {
        val q = query.trim()
        return q.isNotEmpty() && stores.none { it.name.trim().equals(q, ignoreCase = true) }
    }
}
