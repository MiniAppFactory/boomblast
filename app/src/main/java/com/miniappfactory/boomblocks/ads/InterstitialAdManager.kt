package com.miniappfactory.boomblocks.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

// Faz 39: RewardedAdManager ile ayni fire-and-forget desen. Odul yok — bolumler
// arasi zorunlu gecis reklami, kullanici sadece kapatinca (izleyip izlememesi
// fark etmeksizin) onProceed cagrilir; reklam yuklenemezse (no-fill/ag hatasi)
// oyun akisi ASLA bloklanmaz, onProceed yine de cagrilir.
object InterstitialAdManager {

    // Faz 108: yeniden-giris korumasi.
    //
    // Sorun: load() ile show() arasinda 1-3 saniye sessizlik var — hicbir
    // spinner yok. Oyuncunun bu sirada butona TEKRAR basmasi dogal bir tepki
    // (ayni desen Faz 43 ve 48'de rewarded tarafinda zaten kullanici
    // sikayeti olarak gelmisti). Her dokunus ayri bir load() basliyordu:
    // iki tam ekran reklam PES PESE aciliyor ve onProceed iki kez calisip
    // cift popBackStack / cift resetGame yapiyordu. Pes pese interstitial
    // dogrudan bir AdMob yerlesim politikasi ihlalidir.
    //
    // Cozum: ucusta bir gosterim varken gelen cagri SESSIZCE DUSURULUR —
    // onProceed cagrilmaz, cunku birinci cagrinin onProceed'i akisi zaten
    // surdurecek; ikisini de cagirmak cift navigasyon demek olurdu.
    @Volatile
    private var inFlightSince = 0L

    // Guvenlik supabi: SDK callback'i hic gelmezse (uretici hatasi, surec
    // olumu) bayrak sonsuza kadar takili kalir ve TUM ilerleme butonlari
    // sessizce olur — oyuncuyu kilitlememe ilkesinin tam tersi. Bu sure
    // sonunda bayrak bayat sayilir ve yeni cagri normal isler.
    private const val STALE_AFTER_MS = 15_000L

    fun loadAndShow(
        context: Context,
        activity: Activity,
        onProceed: () -> Unit
    ) {
        val now = SystemClock.elapsedRealtime()
        val busy = inFlightSince != 0L && (now - inFlightSince) < STALE_AFTER_MS
        if (busy) return

        // Faz 108: riza kapisi. canRequestAds() false iken (AB'de riza
        // alinmamis/geri cekilmis) reklam ISTENMEZ — ama oyuncu yine de
        // ilerler. Reklamsiz akis her zaman calisan akistir.
        if (!AdsConsent.canRequestAds) {
            onProceed()
            return
        }

        inFlightSince = now
        var finished = false
        val finish = {
            if (!finished) {
                finished = true
                inFlightSince = 0L
                onProceed()
            }
        }

        InterstitialAd.load(
            context,
            AdIds.interstitialAdUnitId(),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialAd.fullScreenContentCallback = object :
                        com.google.android.gms.ads.FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() = finish()
                        override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) = finish()
                    }
                    interstitialAd.show(activity)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    finish()
                }
            }
        )
    }
}
