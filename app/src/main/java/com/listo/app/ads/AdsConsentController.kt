package com.listo.app.ads

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** What the UI needs to know about ads consent. */
data class AdsConsentState(
    val canShowAds: Boolean = false,
    val privacyOptionsRequired: Boolean = false
)

/**
 * Keeps ads and consent in step: the ads SDK is started only once the user's
 * choice allows it, and never more than once. Collected by [com.listo.app.ui.components.AdBanner]
 * so no banner is requested before then.
 */
@Singleton
class AdsConsentController @Inject constructor(
    private val gateway: ConsentGateway,
    private val ads: AdsSdk
) {

    private val _state = MutableStateFlow(AdsConsentState())
    val state: StateFlow<AdsConsentState> = _state.asStateFlow()

    private var sdkStarted = false

    /** Refreshes consent on app start, showing the form where one is required. */
    fun requestConsent(activity: Activity) {
        gateway.requestConsentUpdate(activity) { onConsentFinished() }
    }

    /** Lets the user change an earlier choice; only offered where it is required. */
    fun showPrivacyOptions(activity: Activity) {
        gateway.showPrivacyOptionsForm(activity) { onConsentFinished() }
    }

    /** Starts the ads SDK if the current choice allows it and publishes the new state. */
    fun onConsentFinished() {
        val canShowAds = gateway.canRequestAds()
        if (canShowAds && !sdkStarted) {
            ads.initialize()
            sdkStarted = true
        }
        _state.value = AdsConsentState(
            canShowAds = canShowAds,
            privacyOptionsRequired = gateway.isPrivacyOptionsRequired()
        )
    }
}
