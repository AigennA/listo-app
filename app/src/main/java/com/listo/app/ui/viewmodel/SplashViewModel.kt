package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.ui.navigation.StartDestination
import com.listo.app.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    prefs: PreferencesManager
) : ViewModel() {

    /** The route to open after the splash; null until the preference has loaded. */
    val nextRoute: StateFlow<String?> = prefs.isOnboardingDone
        .map { StartDestination.afterSplash(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}
