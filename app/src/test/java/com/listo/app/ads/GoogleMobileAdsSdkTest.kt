package com.listo.app.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GoogleMobileAdsSdkTest {

    private val context = mockk<Context>(relaxed = true)
    private val configuration = slot<RequestConfiguration>()

    @Before
    fun setUp() {
        mockkStatic(MobileAds::class)
        every { MobileAds.setRequestConfiguration(capture(configuration)) } just runs
        every { MobileAds.initialize(any()) } just runs
    }

    @After
    fun tearDown() {
        unmockkStatic(MobileAds::class)
    }

    @Test
    fun `initialize limits ads to the PG content rating`() {
        GoogleMobileAdsSdk(context).initialize()

        assertEquals(
            RequestConfiguration.MAX_AD_CONTENT_RATING_PG,
            configuration.captured.maxAdContentRating
        )
    }

    @Test
    fun `initialize applies the content rating before starting the SDK`() {
        GoogleMobileAdsSdk(context).initialize()

        verifyOrder {
            MobileAds.setRequestConfiguration(any())
            MobileAds.initialize(context)
        }
    }
}
