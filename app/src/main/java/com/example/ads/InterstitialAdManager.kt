package com.example.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

// Faz 39: RewardedAdManager ile ayni fire-and-forget desen. Odul yok — bolumler
// arasi zorunlu gecis reklami, kullanici sadece kapatinca (izleyip izlememesi
// fark etmeksizin) onProceed cagrilir; reklam yuklenemezse (no-fill/ag hatasi)
// oyun akisi ASLA bloklanmaz, onProceed yine de cagrilir.
object InterstitialAdManager {
    fun loadAndShow(
        context: Context,
        activity: Activity,
        onProceed: () -> Unit
    ) {
        InterstitialAd.load(
            context,
            AdIds.interstitialAdUnitId(),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialAd.fullScreenContentCallback = object :
                        com.google.android.gms.ads.FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() = onProceed()
                        override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) = onProceed()
                    }
                    interstitialAd.show(activity)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    onProceed()
                }
            }
        )
    }
}
