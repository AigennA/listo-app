package com.listo.app.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds

/** Starts Google Mobile Ads. Called only after consent allows ads. */
class GoogleMobileAdsSdk(private val context: Context) : AdsSdk {
    override fun initialize() {
        MobileAds.initialize(context)
    }
}
