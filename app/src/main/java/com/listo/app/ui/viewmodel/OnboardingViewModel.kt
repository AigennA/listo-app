package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.listo.app.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: PreferencesManager
) : ViewModel() {

    /**
     * Suspends until the choice is stored, so the caller can navigate away only once
     * the flag is safely persisted.
     */
    suspend fun markOnboardingDone() = prefs.setOnboardingDone(true)
}
