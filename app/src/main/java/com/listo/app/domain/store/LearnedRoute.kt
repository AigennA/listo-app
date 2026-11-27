package com.listo.app.domain.store

import com.listo.app.data.models.StoreAisle
import com.listo.app.data.models.StoreLayout

/** One item ticked during a trip: which list (trip), which aisle, and when. */
data class AisleTick(val listId: Long, val categoryId: Long, val checkedAt: Long)

/**
 * Learns the order a shopper walks a store's aisles from the order they tick
 * items, trip after trip. Nothing leaves the phone; it only reads tick times.
 */
object LearnedRoute {

    /** Trips needed before an order is trusted. */
    const val MIN_TRIPS = 2

    /**
     * Aisles (category ids) in the order they are usually visited, or empty
     * until there are [MIN_TRIPS] trips that each passed at least two aisles.
     */
    fun order(ticks: List<AisleTick>): List<Long> {
        val trips = ticks.groupBy { it.listId }.values.mapNotNull { trip ->
            val firstVisit = trip.groupBy { it.categoryId }.mapValues { (_, t) -> t.minOf { it.checkedAt } }
            if (firstVisit.size < 2) null else firstVisit.entries.sortedBy { it.value }.map { it.key }
        }
        if (trips.size < MIN_TRIPS) return emptyList()

        // Where in each trip an aisle came, from 0 (first) to 1 (last), averaged over trips.
        val positions = mutableMapOf<Long, MutableList<Double>>()
        trips.forEach { aisles ->
            aisles.forEachIndexed { index, aisle ->
                positions.getOrPut(aisle) { mutableListOf() }.add(index.toDouble() / (aisles.size - 1))
            }
        }
        return positions.entries.sortedBy { it.value.average() }.map { it.key }
    }
}

/** Aisles (category ids) as a shopper meets them on the map: row by row from the top. */
fun StoreLayout.walk(): List<Long> = aisles.sortedWith(compareBy({ it.y }, { it.x })).map { it.categoryId }

/** True when the map already lists the learned [route]'s aisles in that order. */
fun StoreLayout.followsRoute(route: List<Long>): Boolean {
    val known = route.toSet()
    val onMap = aisles.map { it.categoryId }.toSet()
    return walk().filter { it in known } == route.filter { it in onMap }
}

/** Worth suggesting [route]: it covers at least two aisles of this map and the map disagrees. */
fun StoreLayout.shouldOfferRoute(route: List<Long>): Boolean {
    val onMap = aisles.map { it.categoryId }.toSet()
    return route.count { it in onMap } >= 2 && !followsRoute(route)
}

/**
 * The same aisles placed along [route] (aisles the route never saw follow in
 * their current order), on the default aisle spots; entrance and checkout stay.
 */
fun StoreLayout.arrangedBy(route: List<Long>): StoreLayout {
    val current = walk()
    val ordered = route.filter { it in current } + current.filterNot { it in route }
    val slots = DefaultStoreLayout.slots(reserved = setOf(entrance, checkout))
    return copy(aisles = ordered.zip(slots) { id, cell -> StoreAisle(id, cell.x, cell.y) })
}
