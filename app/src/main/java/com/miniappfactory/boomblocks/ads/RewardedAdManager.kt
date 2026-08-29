package com.miniappfactory.boomblocks.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

// Whats-This'teki dogrulanmis desenle ayni: fire-and-forget yukleme, odul
// SADECE gercek SDK "kazanildi" callback'inde verilir (sahte/anlik odul yok).
object RewardedAdManager {

    // Faz 166: in-flight kapisi. `InterstitialAdManager`'daki (Faz 108)
    // `inFlightSince`/`STALE_AFTER_MS` deseninin BIREBIR ayni'si.
    //
    // NEDEN: cokme denetimi, 9 odullu cagri noktasi icinde `watchAdForBooster`in
    // in-flight korumasi OLMAYAN tek yer oldugunu buldu — HUD cipi ciplak bir
    // `clickable`, hicbir "yukleniyor" bayragi yok. Hizli iki dokunus iki
    // BAGIMSIZ `RewardedAd.load` baslatiyor, ikisi de yuklenince arka arkaya
    // IKI tam ekran reklam aciliyor. Bu tam olarak Play'in "disruptive ads"
    // politikasinin hedefledigi davranis, yani yayindan kaldirma riski.
    //
    // Kapiyi cagri noktasina degil BURAYA koymak, dokuz noktayi birden ve
    // kalici olarak kapatir — yarin eklenecek onuncu cagri noktasi da korumali
    // dogar.
    private var inFlightSince = 0L

    // Callback hic gelmezse (SDK hatasi, surec donmasi) kapi sonsuza kadar
    // kilitli kalmasin diye tavan sure. Interstitial ile ayni deger.
    private const val STALE_AFTER_MS = 15_000L

    fun loadAndShow(
        context: Context,
        activity: Activity,
        onRewardEarned: () -> Unit,
        onFailure: () -> Unit,
        // Faz 43: kullanici reklami odulu kazanmadan (video bitmeden) kapatirsa
        // ne onRewardEarned ne onFailure hic tetiklenmiyordu — cagiran taraftaki
        // "yukleniyor" durumu sonsuza kadar takili kalabiliyordu. Bu callback,
        // reklam ekrani ne sekilde kapanirsa kapansin (odullu/odulsuz) HER ZAMAN
        // cagriliyor, SADECE "artik yukleme/gosterim bitti" sinyali icin.
        onAdClosed: () -> Unit = {}
    ) {
        // Faz 108: riza kapisi. canRequestAds() false iken (AB'de riza
        // alinmamis/geri cekilmis) reklam ISTENMEZ. onFailure + onAdClosed
        // yine cagrilir — cagiran taraf bunu no-fill ile ayni sekilde
        // isliyor, yani oyuncu kilitlenmez ve devam hakkini kaybetmez.
        if (!AdsConsent.canRequestAds) {
            onFailure()
            onAdClosed()
            return
        }

        // Faz 166: ag kapisi (kullanicinin buldugu istismarin son savunma hatti).
        // ASIL duzeltme UI tarafinda: ag yokken odullu butonlar hic sunulmuyor
        // (bkz. ConnectivityGate + rememberAdsReachable). Burasi yalnizca
        // "butona basildiktan SONRA ag dustu" yarisini yakalar.
        //
        // Bu dalda bilerek riza kapisiyla AYNI davranis secildi (onFailure +
        // onAdClosed, yani cagiran taraf icin no-fill ile ayni). Gerekce: butona
        // basildigi anda ag VARDI; milisaniyelik bir yaris yuzunden oyuncuyu
        // cezalandirmak Faz 95c/98'de bilerek duzeltilen hatanin ta kendisi
        // olurdu. Istismari kapatan sey UI kapisi, bu satir degil.
        val hasNetwork = NetworkReachability.hasValidatedInternet(context)

        val now = SystemClock.elapsedRealtime()
        val busy = inFlightSince != 0L && (now - inFlightSince) < STALE_AFTER_MS

        if (!ConnectivityGate.shouldAttemptLoad(
                hasNetwork = hasNetwork,
                consentGiven = true, // yukaridaki riza kapisindan gecildi
                loadInFlight = busy
            )
        ) {
            if (busy) {
                // Yinelenen dokunus SESSIZCE yutulur: ilk yukleme hala ayakta ve
                // callback'lerini teslim edecek. Burada onFailure cagirmak,
                // devam akisinda (onFailure -> onDenied -> "izlemis sayilir")
                // oyuncuya BEDAVA bir devam hakki yazardi.
                return
            }
            onFailure()
            onAdClosed()
            return
        }

        inFlightSince = now

        // Kapiyi acan tek yol: hangi dalda biterse bitsin bir kez calisir.
        var released = false
        val release = {
            if (!released) {
                released = true
                inFlightSince = 0L
            }
        }

        RewardedAd.load(
            context,
            AdIds.rewardedAdUnitId(),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            release()
                            onAdClosed()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            release()
                            onAdClosed()
                        }
                    }
                    rewardedAd.show(activity) { onRewardEarned() }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    release()
                    onFailure()
                    onAdClosed()
                }
            }
        )
    }
}
