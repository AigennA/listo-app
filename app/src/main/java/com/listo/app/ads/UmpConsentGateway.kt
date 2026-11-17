package com.listo.app.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * [ConsentGateway] backed by Google's User Messaging Platform, which shows the
 * GDPR consent form configured in the AdMob console.
 */
class UmpConsentGateway(context: Context) : ConsentGateway {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    override fun canRequestAds(): Boolean = consentInformation.canRequestAds()

    override fun isPrivacyOptionsRequired(): Boolean =
        consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    override fun requestConsentUpdate(activity: Activity, onFinished: () -> Unit) {
        consentInformation.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                // Outside the EEA and UK no form is required and this returns at once.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { onFinished() }
            },
            {
                // The update failed (for example no network); ads stay off until it succeeds.
                onFinished()
            }
        )
    }

    override fun showPrivacyOptionsForm(activity: Activity, onFinished: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { onFinished() }
    }
}
