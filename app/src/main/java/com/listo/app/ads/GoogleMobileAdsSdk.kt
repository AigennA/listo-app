package com.listo.app.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

/** Starts Google Mobile Ads. Called only after consent allows ads. */
class GoogleMobileAdsSdk(private val context: Context) : AdsSdk {
    override fun initialize() {
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setMaxAdContentRating(MAX_AD_CONTENT_RATING)
                .build()
        )
        MobileAds.initialize(context)
    }

    private companion object {
        // Teens can install Listo too, so only ads suitable for a general
        // audience with parental guidance are served — never mature ads.
        const val MAX_AD_CONTENT_RATING = RequestConfiguration.MAX_AD_CONTENT_RATING_PG
    }
}
