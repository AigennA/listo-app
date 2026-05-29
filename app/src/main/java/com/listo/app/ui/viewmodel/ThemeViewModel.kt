package com.listo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listo.app.data.models.AccentColor
import com.listo.app.data.models.ThemeMode
import com.listo.app.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ThemeState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.GREEN
)

/**
 * Drives the app-wide theme from persisted preferences. Collected once in
 * [com.listo.app.MainActivity] so theme/accent changes recompose the whole tree.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    prefs: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<ThemeState> = combine(
        prefs.themeMode,
        prefs.accentColor
    ) { mode, accent ->
        ThemeState(themeMode = mode, accentColor = accent)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeState()
    )
}
