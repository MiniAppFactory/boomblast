package com.example.ads

import com.example.BuildConfig

// Faz 4: Gercek AdMob App ID + ad unit ID'leri bu oyun icin ayri bir AdMob
// konsol girisiyle uretilip Faz 6'da buraya konulacak. Gelene kadar
// Google'in herkese acik TEST ID'leriyle calisir (Whats-This'teki
// hardcoded-const + BuildConfig.DEBUG deseniyle birebir ayni).
object AdIds {
    private const val PRODUCTION_BANNER_AD_UNIT_ID = "TODO_REAL_BANNER_AD_UNIT_ID"
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

    private const val PRODUCTION_REWARDED_AD_UNIT_ID = "TODO_REAL_REWARDED_AD_UNIT_ID"
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    fun bannerAdUnitId(): String = if (BuildConfig.DEBUG) TEST_BANNER_AD_UNIT_ID else PRODUCTION_BANNER_AD_UNIT_ID
    fun rewardedAdUnitId(): String = if (BuildConfig.DEBUG) TEST_REWARDED_AD_UNIT_ID else PRODUCTION_REWARDED_AD_UNIT_ID
}
