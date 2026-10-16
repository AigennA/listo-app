package com.listo.app.ads

import android.app.Activity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdsConsentControllerTest {

    private val ads = FakeAdsSdk()

    @Test
    fun `ads stay off until consent has been resolved`() {
        val controller = AdsConsentController(FakeConsentGateway(allowsAds = true), ads)

        assertFalse(controller.state.value.canShowAds)
        assertEquals(0, ads.initializeCount)
    }

    @Test
    fun `consent that allows ads starts the sdk exactly once`() {
        val controller = AdsConsentController(FakeConsentGateway(allowsAds = true), ads)

        controller.onConsentFinished()
        controller.onConsentFinished()

        assertTrue(controller.state.value.canShowAds)
        assertEquals(1, ads.initializeCount)
    }

    @Test
    fun `consent that denies ads keeps the sdk off`() {
        val controller = AdsConsentController(FakeConsentGateway(allowsAds = false), ads)

        controller.onConsentFinished()

        assertFalse(controller.state.value.canShowAds)
        assertEquals(0, ads.initializeCount)
    }

    @Test
    fun `sdk starts as soon as consent is granted later`() {
        val gateway = FakeConsentGateway(allowsAds = false)
        val controller = AdsConsentController(gateway, ads)
        controller.onConsentFinished()

        gateway.allowsAds = true
        controller.onConsentFinished()

        assertTrue(controller.state.value.canShowAds)
        assertEquals(1, ads.initializeCount)
    }

    @Test
    fun `privacy options follow the consent gateway`() {
        val gateway = FakeConsentGateway(allowsAds = true, requiresPrivacyOptions = true)
        val controller = AdsConsentController(gateway, ads)

        controller.onConsentFinished()

        assertTrue(controller.state.value.privacyOptionsRequired)
    }

    private class FakeAdsSdk : AdsSdk {
        var initializeCount = 0
        override fun initialize() {
            initializeCount++
        }
    }

    private class FakeConsentGateway(
        var allowsAds: Boolean = true,
        var requiresPrivacyOptions: Boolean = false
    ) : ConsentGateway {
        override fun canRequestAds() = allowsAds
        override fun isPrivacyOptionsRequired() = requiresPrivacyOptions
        override fun requestConsentUpdate(activity: Activity, onFinished: () -> Unit) = onFinished()
        override fun showPrivacyOptionsForm(activity: Activity, onFinished: () -> Unit) = onFinished()
    }
}
