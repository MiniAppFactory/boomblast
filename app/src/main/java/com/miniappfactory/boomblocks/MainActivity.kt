package com.miniappfactory.boomblocks

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miniappfactory.boomblocks.notifications.ReminderWorker
import com.miniappfactory.boomblocks.ui.BlastViewModel
import com.miniappfactory.boomblocks.ui.navigation.AppNavigation
import com.miniappfactory.boomblocks.ui.retro.RetroViewModel
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.BlastTheBlocksTheme
import com.miniappfactory.boomblocks.ads.AdIds
import com.miniappfactory.boomblocks.ui.theme.blastPalette
import com.miniappfactory.boomblocks.utils.SoundManager
import com.miniappfactory.boomblocks.utils.TextToSpeechManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class MainActivity : ComponentActivity() {
    private val viewModel: BlastViewModel by viewModels()
    // Faz 78: Pro Mode'daki BlastViewModel deseniyle ayni — TEK instance,
    // Activity omru boyunca yasar, tum Retro route'lari arasinda paylasilir
    // (Compose Navigation'in route-basina-yeni-instance varsayilanindan
    // KACINMAK icin — aksi halde Menu'den Game'e gecince motor/ayarlar sifirlanirdi).
    private val retroViewModel: RetroViewModel by viewModels()
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
        // Faz 58: kullanici "telefonun sesini kissam da oyunun sesi kisilmiyor"
        // dedi. Kok neden: SoundManager/SoundPool USAGE_MEDIA+CONTENT_TYPE_MUSIC
        // ile STREAM_MUSIC'e cikiyor ama Activity hangi akisi donanim ses
        // tuslarina baglayacagini hic belirtmiyordu — Android, aktif bir medya
        // oturumu yoksa ses tuslarini varsayilan (zil/bildirim) akisina
        // yonlendirir. `volumeControlStream` STREAM_MUSIC'e sabitlenince ses
        // tuslari HER ZAMAN oyunun gercekten kullandigi akisi kontrol eder.
        volumeControlStream = android.media.AudioManager.STREAM_MUSIC
        // Faz 38: gelistirici/ekip cihazlarinda RELEASE build test edilirken
        // gercek kendi reklamlarimizi izlemis/tiklamis olmayalim diye (AdMob
        // "invalid traffic" politikasi) — bu cihazlara her zaman guvenli
        // test/house reklamlari gosterilir (bkz. AdIds.developerTestDeviceIds).
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(AdIds.developerTestDeviceIds)
                .build()
        )
        MobileAds.initialize(applicationContext) {}
        // Faz 34: kombo ovgu kelimelerini (Good!/Great!/Amazing! vb.) sesli
        // soylemek icin — asenkron init, TTS motoru hazir olana kadar
        // speakPraise() sessizce no-op kalir (bkz. TextToSpeechManager).
        TextToSpeechManager.init(applicationContext)
        // Faz 49: SoundManager artik SoundPool ile gercek SFX dosyalari
        // caliyor (onceden elle sentezliyordu, context gerektirmiyordu) —
        // TextToSpeechManager ile ayni desen: uygulama basinda bir kez init.
        SoundManager.init(applicationContext)
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
                        AppNavigation(viewModel = viewModel, retroViewModel = retroViewModel, adsConsentResolved = consentResolved)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        TextToSpeechManager.shutdown()
        SoundManager.release()
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
