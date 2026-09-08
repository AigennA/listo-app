package com.listo.app.ui.navigation

/**
 * Where the splash screen hands over to. Onboarding is a one-time introduction, so
 * it is only shown until the user has finished or skipped it.
 */
object StartDestination {

    fun afterSplash(isOnboardingDone: Boolean): String =
        if (isOnboardingDone) Screen.Home.route else Screen.Onboarding.route
}
