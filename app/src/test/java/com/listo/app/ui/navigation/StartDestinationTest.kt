package com.listo.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class StartDestinationTest {

    @Test
    fun `first launch goes to onboarding`() {
        assertEquals(
            Screen.Onboarding.route,
            StartDestination.afterSplash(isOnboardingDone = false)
        )
    }

    @Test
    fun `later launches skip onboarding and open the lists`() {
        assertEquals(
            Screen.Home.route,
            StartDestination.afterSplash(isOnboardingDone = true)
        )
    }
}
