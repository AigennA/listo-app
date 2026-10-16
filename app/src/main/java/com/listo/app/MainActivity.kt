package com.listo.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.listo.app.utils.LocaleHelper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.listo.app.ads.AdsConsentController
import com.listo.app.data.models.ThemeMode
import com.listo.app.ui.navigation.LisToNavigation
import com.listo.app.ui.theme.LisToTheme
import com.listo.app.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adsConsent: AdsConsentController

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Asks for consent where it is required; ads start only if it is granted.
        adsConsent.requestConsent(this)
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeState by themeViewModel.uiState.collectAsStateWithLifecycle()

            val darkTheme = when (themeState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT  -> false
                ThemeMode.DARK   -> true
            }

            LisToTheme(darkTheme = darkTheme, accent = themeState.accentColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LisToNavigation()
                }
            }
        }
    }
}
