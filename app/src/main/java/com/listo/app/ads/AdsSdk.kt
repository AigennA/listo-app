package com.listo.app.ads

/**
 * The ads SDK, behind an interface so [AdsConsentController] can be unit-tested
 * without starting Google Mobile Ads.
 */
interface AdsSdk {
    fun initialize()
}
