package com.listo.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.listo.app.ui.viewmodel.AdsViewModel

/**
 * Google's TEST banner ad unit. Replace with your real AdMob banner unit id
 * (and the app id in AndroidManifest) before publishing to Google Play.
 */
private const val TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = TEST_BANNER_UNIT_ID,
    adsViewModel: AdsViewModel = hiltViewModel()
) {
    // AdView cannot render inside Compose previews / inspection mode.
    if (LocalInspectionMode.current) {
        return
    }
    // No ad is requested before the user's consent allows it.
    val adsState by adsViewModel.state.collectAsStateWithLifecycle()
    if (!adsState.canShowAds) {
        return
    }
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
