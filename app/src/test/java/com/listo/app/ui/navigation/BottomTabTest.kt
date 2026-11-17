package com.listo.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomTabTest {

    @Test
    fun `lists is the first tab`() {
        assertEquals(Screen.Home.route, BottomTab.entries.first().route)
    }

    @Test
    fun `there are at most five tabs`() {
        // Material guidance: a navigation bar holds three to five destinations.
        assertTrue(BottomTab.entries.size in 3..5)
    }

    @Test
    fun `every top-level screen shows the bar`() {
        listOf(Screen.Home, Screen.Categories, Screen.Calendar, Screen.History, Screen.Settings)
            .forEach { assertTrue(it.route, BottomTab.showsBar(it.route)) }
    }

    @Test
    fun `detail and start-up screens hide the bar`() {
        listOf(
            Screen.Splash.route,
            Screen.Onboarding.route,
            Screen.ListDetail.route,
            Screen.CategoryItems.route
        ).forEach { assertFalse(it, BottomTab.showsBar(it)) }
    }

    @Test
    fun `no route yet hides the bar`() {
        assertFalse(BottomTab.showsBar(null))
    }

    @Test
    fun `a route maps back to its tab`() {
        assertEquals(BottomTab.Calendar, BottomTab.forRoute(Screen.Calendar.route))
        assertNull(BottomTab.forRoute(Screen.ListDetail.route))
    }
}
