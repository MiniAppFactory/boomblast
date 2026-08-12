package com.miniappfactory.boomblocks.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

// Whats-This'teki dogrulanmis desenle ayni: fire-and-forget yukleme, odul
// SADECE gercek SDK "kazanildi" callback'inde verilir (sahte/anlik odul yok).
object RewardedAdManager {
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
        RewardedAd.load(
            context,
            AdIds.rewardedAdUnitId(),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() = onAdClosed()
                        override fun onAdFailedToShowFullScreenContent(error: AdError) = onAdClosed()
                    }
                    rewardedAd.show(activity) { onRewardEarned() }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    onFailure()
                    onAdClosed()
                }
            }
        )
    }
}
