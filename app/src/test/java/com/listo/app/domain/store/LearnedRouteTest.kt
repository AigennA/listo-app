package com.listo.app.domain.store

import com.listo.app.data.models.GridCell
import com.listo.app.data.models.StoreAisle
import com.listo.app.data.models.StoreLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearnedRouteTest {

    private fun tick(list: Long, category: Long, at: Long) = AisleTick(listId = list, categoryId = category, checkedAt = at)

    // Two trips through the same store: produce, then bread, then dairy.
    private val twoTrips = listOf(
        tick(1, PRODUCE, 10), tick(1, PRODUCE, 12), tick(1, BAKERY, 20), tick(1, DAIRY, 30),
        tick(2, PRODUCE, 100), tick(2, BAKERY, 110), tick(2, DAIRY, 120)
    )

    @Test
    fun `the order the aisles were visited in is learned`() {
        assertEquals(listOf(PRODUCE, BAKERY, DAIRY), LearnedRoute.order(twoTrips))
    }

    @Test
    fun `one trip is not enough to learn from`() {
        assertTrue(LearnedRoute.order(twoTrips.filter { it.listId == 1L }).isEmpty())
    }

    @Test
    fun `an aisle visited at a different point each time lands in between`() {
        val trips = twoTrips + listOf(tick(3, DAIRY, 200), tick(3, PRODUCE, 210), tick(3, BAKERY, 220))
        // Dairy: last, last, first -> middle-ish; produce first, first, middle.
        val order = LearnedRoute.order(trips)
        assertEquals(PRODUCE, order.first())
        assertEquals(3, order.size)
    }

    @Test
    fun `trips with a single aisle teach nothing`() {
        val trips = listOf(tick(1, PRODUCE, 1), tick(2, PRODUCE, 2), tick(3, DAIRY, 3))
        assertTrue(LearnedRoute.order(trips).isEmpty())
    }

    @Test
    fun `aisles are laid out along the learned order, entrance and checkout kept`() {
        val layout = StoreLayout(
            aisles = listOf(StoreAisle(DAIRY, 0, 1), StoreAisle(BAKERY, 2, 1), StoreAisle(PRODUCE, 4, 1), StoreAisle(PETS, 0, 3)),
            entrance = GridCell(0, 0),
            checkout = GridCell(5, 8)
        )
        val arranged = layout.arrangedBy(listOf(PRODUCE, BAKERY, DAIRY))
        assertEquals(listOf(PRODUCE, BAKERY, DAIRY, PETS), arranged.walk())
        assertEquals(layout.entrance, arranged.entrance)
        assertEquals(layout.checkout, arranged.checkout)
        assertEquals(arranged.aisles.size, arranged.aisles.map { it.cell }.toSet().size)
    }

    @Test
    fun `a map already in the learned order needs no change`() {
        val layout = StoreLayout(listOf(StoreAisle(PRODUCE, 0, 1), StoreAisle(BAKERY, 2, 1), StoreAisle(DAIRY, 4, 1)))
        assertTrue(layout.followsRoute(listOf(PRODUCE, BAKERY, DAIRY)))
        assertFalse(layout.followsRoute(listOf(DAIRY, BAKERY, PRODUCE)))
    }

    @Test
    fun `the map is offered the route only when it is learned and differs`() {
        val layout = StoreLayout(listOf(StoreAisle(DAIRY, 0, 1), StoreAisle(PRODUCE, 2, 1)))
        assertTrue(layout.shouldOfferRoute(listOf(PRODUCE, DAIRY)))
        assertFalse(layout.shouldOfferRoute(listOf(DAIRY, PRODUCE)))
        assertFalse(layout.shouldOfferRoute(emptyList()))
        assertFalse(layout.shouldOfferRoute(listOf(PRODUCE)))
    }

    @Test
    fun `aisles the route never saw do not count against the map`() {
        val layout = StoreLayout(listOf(StoreAisle(PETS, 0, 1), StoreAisle(PRODUCE, 2, 1), StoreAisle(DAIRY, 4, 1)))
        assertTrue(layout.followsRoute(listOf(PRODUCE, DAIRY)))
    }

    private companion object {
        const val PRODUCE = 1L
        const val BAKERY = 2L
        const val DAIRY = 3L
        const val PETS = 9L
    }
}
