package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifications.ReminderWorker
import com.example.ui.BlastViewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.BlastSkin
import com.example.ui.theme.BlastTheBlocksTheme
import com.example.ui.theme.blastPalette
import com.example.utils.TextToSpeechManager
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class MainActivity : ComponentActivity() {
    private val viewModel: BlastViewModel by viewModels()
    private val adsConsentResolved = mutableStateOf(false)

    // Faz 27: Android 13+ bildirim izni sonucundan bagimsiz olarak zamanlama
    // zinciri baslatilir — izin verilmezse NotificationHelper zaten sessizce
    // hicbir sey gondermez (bkz. showReminder icindeki izin kontrolu).
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ReminderWorker.ensureScheduled(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(applicationContext) {}
        // Faz 34: kombo ovgu kelimelerini (Good!/Great!/Amazing! vb.) sesli
        // soylemek icin — asenkron init, TTS motoru hazir olana kadar
        // speakPraise() sessizce no-op kalir (bkz. TextToSpeechManager).
        TextToSpeechManager.init(applicationContext)
        // Faz 27: rastgele araliklarla "geri gel" hatirlatma bildirimi zinciri.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            ReminderWorker.ensureScheduled(applicationContext)
        }
        requestAndShowUmpConsentIfRequired { adsConsentResolved.value = true }
        // Faz 25: UMP consent akisi hem basari hem hata dalinda "onResolved"
        // cagirmayabiliyordu (ornegin consent update istegi agdan dolayi
        // basarisiz olur VE canRequestAds() henuz false ise) — bu durumda
        // adsConsentResolved sonsuza kadar false kalip banner reklam O OTURUM
        // BOYUNCA HIC gorunmuyordu (kullanici geri bildirimi: "banner yok").
        // Guvenlik agi: 4 saniyede hala cozulmediyse reklamlari zorla ac.
        Handler(Looper.getMainLooper()).postDelayed({
            if (!adsConsentResolved.value) adsConsentResolved.value = true
        }, 4000)

        setContent {
            val progress by viewModel.playerProgress.collectAsStateWithLifecycle()
            val consentResolved by adsConsentResolved
            val skin = BlastSkin.fromId(progress.uiSkin)
            val backgroundColor = blastPalette(skin, progress.darkMode).background
            BlastTheBlocksTheme(darkTheme = progress.darkMode) {
                // Faz 24: onceden TUM icerik (arka plan dahil) sistem cubuklarindan
                // padding ile itiliyordu — bu da durum/navigasyon cubugu ARKASINDA
                // oyunun rengi degil, cihazin varsayilan SIYAH zemininin gorunmesine
                // yol aciyordu ("ekranı kaplamıyor" — kullanici S20'de fark etti,
                // gercek cihazda uiautomator ile dogrulandi: icerik tam olarak sistem
                // cubugu sinirinda bitiyor ama arkasi renksiz kaliyor). Artik arka
                // plan katmani PADDING'SIZ, tum fiziksel ekrani (sistem cubuklarinin
                // ARKASI dahil) kapliyor; sadece etkilesimli icerik (butonlar, banner
                // reklam) dokunulabilir kalmasi icin sistem cubuklarindan icerlek.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.systemBars)
                    ) {
                        AppNavigation(viewModel = viewModel, adsConsentResolved = consentResolved)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        TextToSpeechManager.shutdown()
        super.onDestroy()
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
