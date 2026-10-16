package com.listo.app.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.listo.app.ads.AdsConsentController
import com.listo.app.ads.AdsConsentState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Exposes the ads consent state to Compose and reopens the consent form on request. */
@HiltViewModel
class AdsViewModel @Inject constructor(
    private val controller: AdsConsentController
) : ViewModel() {

    val state: StateFlow<AdsConsentState> = controller.state

    fun showPrivacyOptions(activity: Activity) {
        controller.showPrivacyOptions(activity)
    }
}
