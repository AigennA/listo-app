package com.listo.app.ads

import android.app.Activity

/**
 * The consent SDK (Google's User Messaging Platform), behind an interface so the
 * rule "no ads before consent" can be unit-tested without a device.
 */
interface ConsentGateway {

    /** Whether the user's current choice allows requesting ads. */
    fun canRequestAds(): Boolean

    /** True where the user must be able to change the choice later (EEA and UK). */
    fun isPrivacyOptionsRequired(): Boolean

    /** Refreshes the stored consent and shows the consent form when one is required. */
    fun requestConsentUpdate(activity: Activity, onFinished: () -> Unit)

    /** Reopens the consent form so the user can change an earlier choice. */
    fun showPrivacyOptionsForm(activity: Activity, onFinished: () -> Unit)
}
