package com.example.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
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
        onFailure: () -> Unit
    ) {
        RewardedAd.load(
            context,
            AdIds.rewardedAdUnitId(),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    rewardedAd.show(activity) { onRewardEarned() }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    onFailure()
                }
            }
        )
    }
}
