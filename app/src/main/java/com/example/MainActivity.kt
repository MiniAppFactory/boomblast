package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BlastViewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.BlastTheBlocksTheme
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class MainActivity : ComponentActivity() {
    private val viewModel: BlastViewModel by viewModels()
    private val adsConsentResolved = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(applicationContext) {}
        requestAndShowUmpConsentIfRequired { adsConsentResolved.value = true }

        setContent {
            val progress by viewModel.playerProgress.collectAsStateWithLifecycle()
            val consentResolved by adsConsentResolved
            BlastTheBlocksTheme(darkTheme = progress.darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel, adsConsentResolved = consentResolved)
                }
            }
        }
    }

    // Whats-This'te dogrulanmis, gercek cihazda calisan UMP deseni: consent
    // guncelleme HATA verse bile canRequestAds() true ise reklamlara devam
    // edilir (kok neden: AdMob konsolunda Privacy & Messaging formu henuz
    // yayinlanmamissa hata donuyor ama bu, reklam istemeyi engellememeli).
    private fun requestAndShowUmpConsentIfRequired(onResolved: () -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { onResolved() }
            },
            {
                if (consentInformation.canRequestAds()) {
                    onResolved()
                }
            }
        )
    }
}
